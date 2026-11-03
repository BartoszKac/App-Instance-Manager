section() { printf '\n@@%s\n' "$1"; }
has() { command -v "$1" >/dev/null 2>&1; }
section hostname; hostname 2>/dev/null
section os; if [ -r /etc/os-release ]; then . /etc/os-release; echo "$PRETTY_NAME"; elif has sw_vers; then echo "$(sw_vers -productName) $(sw_vers -productVersion)"; else uname -s; fi
section kernel; uname -sr 2>/dev/null
section arch; uname -m 2>/dev/null
section uptime; uptime -p 2>/dev/null || uptime 2>/dev/null
section boot; uptime -s 2>/dev/null || who -b 2>/dev/null
section virt; has systemd-detect-virt && systemd-detect-virt 2>/dev/null
section model; if [ -r /sys/class/dmi/id/product_name ]; then echo "$(cat /sys/class/dmi/id/sys_vendor 2>/dev/null) $(cat /sys/class/dmi/id/product_name 2>/dev/null)"; elif [ -r /proc/device-tree/model ]; then tr -d '\0' < /proc/device-tree/model; elif has sysctl; then sysctl -n hw.model 2>/dev/null; fi
section cpu; if has lscpu; then lscpu 2>/dev/null | sed -n 's/^Model name: *//p' | head -1; elif [ -r /proc/cpuinfo ]; then sed -n 's/^model name[^:]*: *//p' /proc/cpuinfo | head -1; elif has sysctl; then sysctl -n machdep.cpu.brand_string 2>/dev/null; fi
section cores; if has nproc; then nproc 2>/dev/null; elif has sysctl; then sysctl -n hw.ncpu 2>/dev/null; fi
section load; cat /proc/loadavg 2>/dev/null | cut -d' ' -f1-3 || sysctl -n vm.loadavg 2>/dev/null
section memory; if has free; then free -h 2>/dev/null; elif has sysctl; then echo "$(( $(sysctl -n hw.memsize 2>/dev/null) / 1073741824 )) GiB"; fi
section disks; df -hP -x tmpfs -x devtmpfs -x squashfs -x overlay 2>/dev/null || df -h 2>/dev/null
section blocks; has lsblk && lsblk -o NAME,SIZE,TYPE,FSTYPE,MOUNTPOINT 2>/dev/null
section mounts; if has findmnt; then findmnt -rno TARGET,SOURCE,FSTYPE 2>/dev/null; else mount 2>/dev/null; fi
section gpu; has lspci && lspci 2>/dev/null | grep -Ei 'vga|3d controller' | cut -d' ' -f2-
section ip; if has ip; then ip -4 -o addr show scope global 2>/dev/null | awk '{print $2 " " $4}'; elif has ifconfig; then ifconfig 2>/dev/null | awk '/inet /{print $2}'; fi
section mac; has ip && ip -o link show 2>/dev/null | awk '$2!="lo:"{for(i=1;i<=NF;i++) if($i=="link/ether") print $2 " " $(i+1)}'
section routes; if has ip; then ip route show 2>/dev/null; elif has netstat; then netstat -rn 2>/dev/null; fi
section dns; sed -n 's/^nameserver *//p' /etc/resolv.conf 2>/dev/null
section listening; if has ss; then ss -tulnH 2>/dev/null | awk '{print $1 " " $5}' | sort -u; elif has netstat; then netstat -an 2>/dev/null | grep LISTEN | awk '{print $4}' | sort -u; fi
section running; has systemctl && systemctl list-units --type=service --state=running --no-legend --no-pager 2>/dev/null | awk '{print ($1=="●" ? $2 : $1)}'
section unitfiles; has systemctl && systemctl list-unit-files --type=service --no-legend --no-pager 2>/dev/null | awk '{print $1 " " $2}'
section timers; has systemctl && systemctl list-timers --all --no-legend --no-pager 2>/dev/null | awk '{print $(NF-1) " " $NF}'
section cron; has crontab && crontab -l 2>/dev/null | grep -Ev '^[[:space:]]*(#|$)|^[A-Za-z_][A-Za-z0-9_]*=' | awk '{print $1, $2, $3, $4, $5, $6}'
section processes; ps -eo user,pid,pcpu,pmem,comm --sort=-pcpu 2>/dev/null | head -150 || ps -axo user,pid,pcpu,pmem,comm 2>/dev/null | head -150
section docker; has docker && docker --version 2>/dev/null
section containers; has docker && docker ps -a --format '{{.Names}} ({{.Image}}): {{.Status}}' 2>/dev/null
section images; has docker && docker images --format '{{.Repository}}:{{.Tag}} {{.Size}}' 2>/dev/null
section volumes; has docker && docker volume ls --format '{{.Name}}' 2>/dev/null
section runtimes; for t in python3 node gcc g++ go nginx apache2 mysql psql php ruby; do has "$t" && echo "$t: $("$t" --version 2>&1 | head -1)"; done; has java && echo "java: $(java -version 2>&1 | head -1)"
section packages; if has dpkg-query; then dpkg-query -f '${Package} ${Version}\n' -W 2>/dev/null; elif has rpm; then rpm -qa 2>/dev/null | sort; elif has apk; then apk info -v 2>/dev/null; elif has brew; then brew list --versions 2>/dev/null; fi
section accounts; if has getent; then getent passwd; else cat /etc/passwd 2>/dev/null; fi | awk -F: '{print $1 " uid=" $3 " home=" $6 " shell=" $7}'
section usergroups; if has getent; then getent group; else cat /etc/group 2>/dev/null; fi | awk -F: '{print $1 " gid=" $3 " " $4}'
section whoami; id 2>/dev/null
section users; who 2>/dev/null | awk '{print $1 " " $2}' | sort -u
section logins; has last && last -n 20 2>/dev/null | awk 'NF{print $1, $3, $4, $5, $6, $7}'
exit 0
