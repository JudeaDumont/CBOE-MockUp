1. Open the project in IntelliJ
2. Install docker desktop
3. Install minicube from https://minikube.sigs.k8s.io/docs/start/?arch=%2Fwindows%2Fx86-64%2Fstable%2F.exe+download
    - this will install kubernetes

4. This is a work in progress for docker compose stuff: docker-compose -f docker-compose.yml -f experimental/kafka-test/docker-compose-kafka-test.yml up --build

5. npm run dev

To get the project running:
- docker-compose up --build in the root directory "docker-compose.yml"
- this should be done in intelliJ where you can tell it to sync with the docker-compose file
- then run the services 
  - UserDetailsServiceApplication
  - UserServiceApplication
  - IndexConsumerApplication
  - IndexProducerApplication
- Then go into the services/front-end-service/front-end directory
  - and run: npm run dev
  - then you should be able to go to http://localhost:5173/
  - create an account, login, and view a high speed ticker that uses kafka and react websocket.