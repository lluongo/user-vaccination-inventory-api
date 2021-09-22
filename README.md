# user-vaccination-inventory-api

La aplicacion esta construida bajo estas tecnologias :

	--> Keycloak como proveedor de seguridad
	--> java lenguajes de desarrollo
	--> postgres como tecnologia de base de datos
	--> Docker y Docker-Compose como tecnologia de contenerizacion

Toda la informacion de la configuracion de cada servicio esta dentro de los archivos :

	docker-compose.yml
	Dockerfile
	user-vaccination-inventory.env
	keycloak-variables.env
	dbpostgres-compose.env



1) Para levantar la app se debe ejecutar el docker compose --> docker-compose.yml 
	- comando = "docker-compose up"

Esta accion levantara 3 el postgres , keycloak server y la applicacion user-vaccination-inventory .

2) Acceder a keycloak : http:localhost:8080/auth --> user: admin pass: admin

3) Crear en Keycloak un real bajo el nombre "kruger"

4) crear client-id llamado --> user-vaccination-inventory

5) dentro del cliente crear  los roles "ROLE_ADMINISTRATOR" Y "EMPLOYEE"

6) Crear usuarios administradores asignado el el rol "ROLE_ADMINISTRATOR"

7) luego se podra pedir token con las credenciales a la url --> http://localhost:8080/auth/realms/kruger/protocol/openid-connect/token

8) se podran consumir los endpoints de la api la cual validara si segun el rol asignado en el token habilita o no el uso de dicho endpoint . En caso de no permitirlo emitira un error 403 por otro lado en caso que el token este vencido, se emite un error 401.

9) luego la api se encargara de crear el usuario localmente con los datos segun las consignas y por otro lado dara de alta automaticamente el usuario en Keycloak con el rol EMPLOYEE para luego poder pedir token y consumir los endpoints.



