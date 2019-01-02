FROM docker.elastic.co/elasticsearch/elasticsearch:5.6.3
ADD ./entrypoint.sh /opt/entrypoint.sh
USER root
RUN chmod a+x /opt/entrypoint.sh
USER elasticsearch
CMD "/opt/entrypoint.sh"