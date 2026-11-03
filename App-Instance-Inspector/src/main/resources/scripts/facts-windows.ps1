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
Section boot $os.LastBootUpTime
Section model "$($cs.Manufacturer) $($cs.Model)"
Section cpu $cpu.Name
Section cores $cpu.NumberOfLogicalProcessors
Section load "$($cpu.LoadPercentage)%"
Section memory ("{0:N1} GiB, free {1:N1} GiB" -f ($os.TotalVisibleMemorySize / 1MB), ($os.FreePhysicalMemory / 1MB))
Section disks (Get-CimInstance Win32_LogicalDisk -Filter 'DriveType=3' | % { "{0} {1:N0} GiB, free {2:N0} GiB" -f $_.DeviceID, ($_.Size / 1GB), ($_.FreeSpace / 1GB) })
Section gpu (Get-CimInstance Win32_VideoController | % Name)
Section ip (Get-NetIPAddress -AddressFamily IPv4 | ? IPAddress -ne '127.0.0.1' | % { "$($_.InterfaceAlias) $($_.IPAddress)/$($_.PrefixLength)" })
Section mac (Get-NetAdapter | ? Status -eq 'Up' | % { "$($_.Name) $($_.MacAddress)" })
Section routes (Get-NetRoute -AddressFamily IPv4 | % { "$($_.DestinationPrefix) $($_.NextHop)" })
Section dns (Get-DnsClientServerAddress -AddressFamily IPv4 | % ServerAddresses | Select -Unique)
Section listening (Get-NetTCPConnection -State Listen | % { "$($_.LocalAddress):$($_.LocalPort)" } | Sort -Unique)
Section running (Get-Service | ? Status -eq 'Running' | % Name)
Section unitfiles (Get-Service | % { "$($_.Name) $($_.StartType)" })
Section timers (Get-ScheduledTask | % { "$($_.TaskPath)$($_.TaskName) $($_.State)" })
Section processes (Get-Process | Sort CPU -Descending | Select -First 150 | % { "$($_.Id) $($_.ProcessName)" })
Section docker (docker --version 2>$null)
Section containers (docker ps -a --format '{{.Names}} ({{.Image}}): {{.Status}}' 2>$null)
Section images (docker images --format '{{.Repository}}:{{.Tag}} {{.Size}}' 2>$null)
Section packages (Get-ItemProperty 'HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*', 'HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*' | ? DisplayName | % { "$($_.DisplayName) $($_.DisplayVersion)" } | Sort -Unique)
Section accounts (Get-LocalUser | % { "$($_.Name) enabled=$($_.Enabled)" })
Section usergroups (Get-LocalGroup | % Name)
Section whoami (whoami)
Section users ((quser 2>$null) | Select -Skip 1)
