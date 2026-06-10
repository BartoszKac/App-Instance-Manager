FROM alpine:3.19

RUN apk add --no-cache \
    openssh \
    openjdk17-jre-headless \
    python3 \
    py3-pip \
    bash \
    curl

RUN ssh-keygen -A

RUN adduser -D -s /bin/bash user && \
    echo "user:user" | chpasswd

RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin no/' /etc/ssh/sshd_config && \
    sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config && \
    sed -i 's/#PubkeyAuthentication yes/PubkeyAuthentication yes/' /etc/ssh/sshd_config

EXPOSE 22

CMD ["/usr/sbin/sshd", "-D"]
