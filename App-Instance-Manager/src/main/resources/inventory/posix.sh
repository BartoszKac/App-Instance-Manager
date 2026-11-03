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
section memory; if has free; then free -h 2>/dev/null | sed -n 's/^Mem: *//p' | awk '{print "razem " $1 ", zajęte " $2 ", dostępne " $NF}'; elif has sysctl; then echo "razem $(( $(sysctl -n hw.memsize 2>/dev/null) / 1073741824 )) GiB"; fi
section swap; has free && free -h 2>/dev/null | sed -n 's/^Swap: *//p' | awk '{print "razem " $1 ", zajęte " $2}'
section disks; df -hP -x tmpfs -x devtmpfs -x squashfs -x overlay 2>/dev/null || df -h 2>/dev/null
section gpu; has lspci && lspci 2>/dev/null | grep -Ei 'vga|3d controller' | cut -d' ' -f2-
section ip; if has ip; then ip -4 -o addr show scope global 2>/dev/null | awk '{print $2 " " $4}'; elif has ifconfig; then ifconfig 2>/dev/null | awk '/inet /{print $2}'; fi
section mac; has ip && ip -o link show 2>/dev/null | awk '$2!="lo:"{for(i=1;i<=NF;i++) if($i=="link/ether") print $2 " " $(i+1)}'
section gateway; has ip && ip route show default 2>/dev/null | head -2
section dns; sed -n 's/^nameserver *//p' /etc/resolv.conf 2>/dev/null
section listening; if has ss; then ss -tlnH 2>/dev/null | awk '{print $4}' | sort -u; elif has netstat; then netstat -an 2>/dev/null | awk '/LISTEN/{print $4}' | sort -u; fi
section services; has systemctl && echo "działające usługi systemd: $(systemctl list-units --type=service --state=running --no-legend --no-pager 2>/dev/null | wc -l)"
section running; has systemctl && systemctl list-units --type=service --state=running --no-legend --no-pager 2>/dev/null | awk '{print ($1=="●" ? $2 : $1)}' | head -60
section docker; has docker && docker --version 2>/dev/null
section containers; has docker && docker ps --format '{{.Names}} ({{.Image}}): {{.Status}}' 2>/dev/null | head -40
section runtimes; for t in java python3 node gcc g++ go nginx apache2 mysql psql; do has "$t" && echo "$t: $("$t" --version 2>&1 | head -1)"; done; has java && echo "java: $(java -version 2>&1 | head -1)"
section users; who 2>/dev/null | awk '{print $1 " " $2}' | sort -u
section packages; if has dpkg-query; then echo "dpkg: $(dpkg-query -f '.\n' -W 2>/dev/null | wc -l)"; elif has rpm; then echo "rpm: $(rpm -qa 2>/dev/null | wc -l)"; fi
exit 0
