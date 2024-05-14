# Pull base image.
FROM ubuntu:20.04

# Install. 
RUN sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list
RUN apt-get update
RUN apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata -y
RUN apt-get install -y build-essential openssh-server net-tools bash-completion
RUN apt-get install -y software-properties-common
RUN apt-get install -y byobu tree curl git htop man unzip vim nano htop wget
RUN rm -rf /var/lib/apt/lists/*
ENV LC_ALL en_GB.UTF-8
ENV LANG en_GB.UTF-8
ENV LANGUAGE en_GB.UTF-8

RUN ssh-keygen -q -t rsa -N '' -f /id_rsa
RUN mkdir /var/run/sshd
RUN echo 'root:Admin1' | chpasswd
RUN sed -i 's/#*PermitRootLogin prohibit-password/PermitRootLogin yes/g' /etc/ssh/sshd_config

# SSH login fix. Otherwise user is kicked off after login
RUN sed -i 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' /etc/pam.d/sshd

ENV NOTVISIBLE="in users profile"
RUN echo "export VISIBLE=now" >> /etc/profile

COPY ./authorized_keys /root/.ssh/authorized_keys

EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]
