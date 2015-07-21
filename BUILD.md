### ensure all dependencies installed locally
    
    mvn dependency:go-offline

### run maven in offline mode
    
    mvn -X -o clean compile package

    ./install.sh
    elasticsearch --config=/usr/local/opt/elasticsearch/config/elasticsearch.yml
