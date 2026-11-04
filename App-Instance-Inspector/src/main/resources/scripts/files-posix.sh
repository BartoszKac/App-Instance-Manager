export LC_ALL=C
if find / -maxdepth 0 -printf '' >/dev/null 2>&1; then
  nice -n 19 find / \( -path /proc -o -path /sys -o -path /dev -o -path /run -o -fstype nfs -o -fstype nfs4 -o -fstype cifs -o -fstype smb3 -o -fstype fuse.sshfs \) -prune -o -printf '%y\t%s\t%u\t%g\t%m\t%T@\t%p\0'
elif stat -c '%s' / >/dev/null 2>&1; then
  nice -n 19 find / \( -path /proc -o -path /sys -o -path /dev -o -path /run \) -prune -o -exec stat -c "%A$(printf '\t')%s$(printf '\t')%U$(printf '\t')%G$(printf '\t')%a$(printf '\t')%Y$(printf '\t')%n" {} + | tr '\n' '\000'
else
  nice -n 19 find / \( -path /proc -o -path /sys -o -path /dev -o -path /run -o -path /System/Volumes \) -prune -o -exec stat -f "%Sp$(printf '\t')%z$(printf '\t')%Su$(printf '\t')%Sg$(printf '\t')%Mp%Lp$(printf '\t')%m$(printf '\t')%N" {} + | tr '\n' '\000'
fi
exit 0
