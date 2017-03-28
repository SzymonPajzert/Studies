run: 
	docker run -v $(dir $(CURDIR)/$(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST)))/traffic_prediction:/home/traffic_prediction -it paddledev/paddle:cpu-latest /bin/bash
