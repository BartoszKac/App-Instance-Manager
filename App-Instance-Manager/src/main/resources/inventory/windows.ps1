$ErrorActionPreference = 'SilentlyContinue'
$ProgressPreference = 'SilentlyContinue'
[Console]::OutputEncoding = [Text.Encoding]::UTF8
function Section($key, $value) { Write-Output "`n@@$key"; if ($value) { $value | Out-String -Width 200 | Write-Output } }
$os = Get-CimInstance Win32_OperatingSystem
$cs = Get-CimInstance Win32_ComputerSystem
$cpu = Get-CimInstance Win32_Processor | Select-Object -First 1
Section hostname $env:COMPUTERNAME
Section os "$($os.Caption) (build $($os.BuildNumber))"
Section kernel $os.Version
Section arch $os.OSArchitecture
Section uptime ("{0:%d} dni {0:%h} godz. {0:%m} min" -f ((Get-Date) - $os.LastBootUpTime))
Section boot $os.LastBootUpTime
Section model "$($cs.Manufacturer) $($cs.Model)"
Section cpu $cpu.Name
Section cores "$($cpu.NumberOfCores) rdzeni, $($cpu.NumberOfLogicalProcessors) wątków"
Section load "$($cpu.LoadPercentage)%"
Section memory ("razem {0:N1} GiB, dostępne {1:N1} GiB" -f ($os.TotalVisibleMemorySize / 1MB), ($os.FreePhysicalMemory / 1MB))
Section disks (Get-CimInstance Win32_LogicalDisk -Filter 'DriveType=3' | ForEach-Object { "{0} {1:N0} GiB, wolne {2:N0} GiB" -f $_.DeviceID, ($_.Size / 1GB), ($_.FreeSpace / 1GB) })
Section gpu (Get-CimInstance Win32_VideoController | ForEach-Object { $_.Name })
Section ip (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -ne '127.0.0.1' } | ForEach-Object { "$($_.InterfaceAlias) $($_.IPAddress)/$($_.PrefixLength)" })
Section mac (Get-NetAdapter | Where-Object { $_.Status -eq 'Up' } | ForEach-Object { "$($_.Name) $($_.MacAddress)" })
Section gateway (Get-NetRoute -DestinationPrefix '0.0.0.0/0' | ForEach-Object { $_.NextHop })
Section dns (Get-DnsClientServerAddress -AddressFamily IPv4 | ForEach-Object { $_.ServerAddresses } | Select-Object -Unique)
Section listening (Get-NetTCPConnection -State Listen | ForEach-Object { "$($_.LocalAddress):$($_.LocalPort)" } | Sort-Object -Unique)
$running = Get-Service | Where-Object { $_.Status -eq 'Running' }
Section services "działające usługi Windows: $(@($running).Count)"
Section running ($running | Select-Object -First 60 | ForEach-Object { $_.Name })
Section docker (docker --version 2>$null)
Section containers (docker ps --format '{{.Names}} ({{.Image}}): {{.Status}}' 2>$null)
Section runtimes (foreach ($t in 'python', 'node', 'gcc') { if (Get-Command $t) { "${t}: $((& $t --version 2>&1) | Select-Object -First 1)" } }; if (Get-Command java) { "java: $((& java -version 2>&1) | Select-Object -First 1)" })
Section users ((quser 2>$null) | Select-Object -Skip 1)
