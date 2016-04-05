# JaqpotQuattro

****What is Jaqpot****
Jaqpot v4 (Quattro) is a REST application developed under the eNanoMapper project (http://www.enanomapper.net) that supports model training and data preprocessing algorithms such as multiple linear regression, support vector machines, neural networks, an implementation of the leverage algorithm for domain of applicability estimation and various data preprocessing algorithms like PLS and scaling.

****How to run Jaqpot using Docker****
You can download the Jaqpot docker image and start a container with the following commands:
`docker pull jaqpot/jaqpot-core`
`docker run -d -p 8080:8080 --net="host" jaqpot/jaqpot-core`

In order to run Jaqpot core services, a local instance of *MongoDB* is needed. 
Jaqpot is configured to find it running on *port: 27017* (Mongo default).
You can run a MongoDB container with the following commands:
`docker pull mongo`
`docker run -d -p 27017:27017 mongo`

After running the containers successfully you can browse Jaqpot API from this url:
http://localhost:8080/jaqpot/swagger/
