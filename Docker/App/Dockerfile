FROM pedrombmachado/ntu_lubuntu:comp20081

COPY id_rsa.pub /home/ntu-user/.ssh/id_rsa.pub
COPY id_rsa /home/ntu-user/.ssh/id_rsa

RUN chown ntu-user:ntu-user /home/ntu-user/.ssh/id_rsa.pub
RUN chown ntu-user:ntu-user /home/ntu-user/.ssh/id_rsa
RUN chmod 600 /home/ntu-user/.ssh/id_rsa.pub
RUN chmod 600 /home/ntu-user/.ssh/id_rsa