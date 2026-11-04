$ErrorActionPreference = 'SilentlyContinue'
$ProgressPreference = 'SilentlyContinue'
[Console]::OutputEncoding = [Text.Encoding]::UTF8
$out = [Console]::Out
function Emit($t, $s, $m, $p) { $out.Write("$t`t$s`t`t`t`t$m`t$p`0") }
foreach ($disk in Get-CimInstance Win32_LogicalDisk -Filter 'DriveType=3') {
  $root = "$($disk.DeviceID)\"
  Emit 'd' 0 0 $root
  Get-ChildItem -LiteralPath $root -Recurse -Force -ErrorVariable denied | ForEach-Object {
    $t = if ($_.Attributes -band [IO.FileAttributes]::ReparsePoint) { 'l' } elseif ($_.PSIsContainer) { 'd' } else { 'f' }
    $s = if ($_.PSIsContainer) { 0 } else { $_.Length }
    Emit $t $s ([DateTimeOffset]$_.LastWriteTimeUtc).ToUnixTimeSeconds() $_.FullName
  }
  foreach ($e in $denied) { if ($e.TargetObject) { Emit '!' 0 0 $e.TargetObject } }
}
$out.Flush()
