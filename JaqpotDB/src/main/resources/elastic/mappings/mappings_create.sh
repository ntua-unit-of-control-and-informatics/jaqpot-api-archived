export elastic=http://localhost:9200/jaqpot
curl -XDELETE $elastic?pretty
curl -XPUT $elastic?pretty
curl -XPUT $elastic/_mapping/_default_?pretty --data-binary @mappings_default.json
curl -XPUT $elastic/_mapping/feature?pretty --data-binary @mappings_feature.json 
curl -XPUT $elastic/_mapping/user?pretty --data-binary @mappings_user.json
curl -XPUT $elastic/_mapping/bibtex?pretty --data-binary @mappings_bibtex.json
