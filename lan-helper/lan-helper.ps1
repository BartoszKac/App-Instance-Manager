$ErrorActionPreference = 'Stop'

$Port = 8899
$AllowedOrigin = 'http://localhost:5173'
$PingTimeoutMs = 800
$ProbeWaitMs = 800
$NameWaitMs = 1000

function ConvertTo-IpNumber([string]$Ip) {
    $bytes = [Net.IPAddress]::Parse($Ip).GetAddressBytes()
    [Array]::Reverse($bytes)
    return [BitConverter]::ToUInt32($bytes, 0)
}

function ConvertFrom-IpNumber([uint32]$Number) {
    $bytes = [BitConverter]::GetBytes($Number)
    [Array]::Reverse($bytes)
    return ([Net.IPAddress]::new($bytes)).ToString()
}

function Test-PrivateIp([string]$Ip) {
    return $Ip -match '^(10\.|192\.168\.|172\.(1[6-9]|2[0-9]|3[01])\.)'
}

function Get-LanSubnet {
    $config = Get-NetIPConfiguration |
        Where-Object { $_.IPv4DefaultGateway -and $_.NetAdapter.Status -eq 'Up' } |
        Select-Object -First 1
    if (-not $config) { return $null }
    $address = @($config.IPv4Address)[0]
    $prefix = [int]$address.PrefixLength
    if (-not (Test-PrivateIp $address.IPAddress) -or $prefix -lt 24 -or $prefix -gt 30) { return $null }
    $mask = [uint32]([Math]::Pow(2, 32) - [Math]::Pow(2, 32 - $prefix))
    $network = (ConvertTo-IpNumber $address.IPAddress) -band $mask
    $hostCount = [Math]::Pow(2, 32 - $prefix) - 2
    return [pscustomobject]@{
        Cidr           = "$(ConvertFrom-IpNumber $network)/$prefix"
        InterfaceIndex = $config.InterfaceIndex
        LocalIp        = $address.IPAddress
        Hosts          = 1..$hostCount | ForEach-Object { ConvertFrom-IpNumber ([uint32]($network + $_)) }
    }
}

function Get-PingResponders([string[]]$Hosts) {
    $pending = foreach ($ip in $Hosts) {
        [pscustomobject]@{ Ip = $ip; Task = ([Net.NetworkInformation.Ping]::new()).SendPingAsync($ip, $PingTimeoutMs) }
    }
    [void][Threading.Tasks.Task]::WaitAll([Threading.Tasks.Task[]]@($pending.Task), $PingTimeoutMs * 3)
    return $pending |
        Where-Object { $_.Task.Status -eq 'RanToCompletion' -and $_.Task.Result.Status -eq 'Success' } |
        ForEach-Object { $_.Ip }
}

function Get-MacByIp($Subnet) {
    $macs = @{}
    Get-NetNeighbor -InterfaceIndex $Subnet.InterfaceIndex -AddressFamily IPv4 -State Reachable, Delay, Probe -ErrorAction SilentlyContinue |
        Where-Object { $Subnet.Hosts -contains $_.IPAddress } |
        ForEach-Object { $macs[$_.IPAddress] = $_.LinkLayerAddress }
    return $macs
}

function Get-OpenSshHosts([string[]]$Hosts) {
    $clients = foreach ($ip in $Hosts) {
        $client = [Net.Sockets.TcpClient]::new()
        [pscustomobject]@{ Ip = $ip; Client = $client; Task = $client.ConnectAsync($ip, 22) }
    }
    Start-Sleep -Milliseconds $ProbeWaitMs
    $open = $clients | Where-Object { $_.Task.Status -eq 'RanToCompletion' } | ForEach-Object { $_.Ip }
    $clients | ForEach-Object { $_.Client.Close() }
    return @($open)
}

function Get-HostNames([string[]]$Hosts) {
    $lookups = foreach ($ip in $Hosts) {
        [pscustomobject]@{ Ip = $ip; Task = [Net.Dns]::GetHostEntryAsync($ip) }
    }
    Start-Sleep -Milliseconds $NameWaitMs
    $names = @{}
    $lookups | Where-Object { $_.Task.Status -eq 'RanToCompletion' -and $_.Task.Result.HostName -ne $_.Ip } |
        ForEach-Object { $names[$_.Ip] = $_.Task.Result.HostName }
    return $names
}

function Invoke-LanScan($Subnet) {
    $responders = @(Get-PingResponders $Subnet.Hosts)
    $macs = Get-MacByIp $Subnet
    $found = @($responders + @($macs.Keys) + $Subnet.LocalIp | Select-Object -Unique |
        Sort-Object { ConvertTo-IpNumber $_ })
    $ssh = Get-OpenSshHosts $found
    $names = Get-HostNames $found
    $devices = @($found | ForEach-Object {
        [ordered]@{ ip = $_; hostname = $names[$_]; macAddress = $macs[$_]; vendor = $null; sshOpen = ($ssh -contains $_) }
    })
    return [ordered]@{ cidr = $Subnet.Cidr; deviceCount = $devices.Count; devices = $devices }
}

function Send-Json($Response, [int]$Status, $Body) {
    $bytes = [Text.Encoding]::UTF8.GetBytes((ConvertTo-Json -InputObject $Body -Depth 4 -Compress))
    $Response.StatusCode = $Status
    $Response.ContentType = 'application/json; charset=utf-8'
    $Response.ContentLength64 = $bytes.Length
    $Response.OutputStream.Write($bytes, 0, $bytes.Length)
}

function Invoke-Route($Path) {
    $subnet = Get-LanSubnet
    if (-not $subnet) { return @{ Status = 503; Body = @{ detail = 'Nie znaleziono prywatnej sieci /24 do skanowania.' } } }
    if ($Path -eq '/subnet') { return @{ Status = 200; Body = @{ cidr = $subnet.Cidr } } }
    return @{ Status = 200; Body = (Invoke-LanScan $subnet) }
}

function Invoke-Request($Context) {
    $request = $Context.Request
    $response = $Context.Response
    try {
        if ($request.Headers['Origin'] -ne $AllowedOrigin) { Send-Json $response 403 @{ detail = 'Niedozwolone pochodzenie.' }; return }
        $response.Headers['Access-Control-Allow-Origin'] = $AllowedOrigin
        $routes = @('/subnet', '/scan')
        if ($request.HttpMethod -ne 'GET' -or $routes -notcontains $request.Url.AbsolutePath) { Send-Json $response 404 @{ detail = 'Nie ma takiej ścieżki.' }; return }
        $result = Invoke-Route $request.Url.AbsolutePath
        Send-Json $response $result.Status $result.Body
    } catch {
        Send-Json $response 500 @{ detail = 'Skan się nie udał.' }
    } finally {
        $response.Close()
    }
}

$listener = [Net.HttpListener]::new()
$listener.Prefixes.Add("http://localhost:$Port/")
$listener.Start()
while ($listener.IsListening) {
    Invoke-Request $listener.GetContext()
}
