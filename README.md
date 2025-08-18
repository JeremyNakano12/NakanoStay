# NakanoStay
Este es el Backend para la aplicación de reserva de hoteles NakanoStay, Este usa Spring como backend junto a Kotlin, como base de datos usa un contenedor de PostgreSQL que es levantado mediante docker-compose.

# Clonar el repositorio

```
git clone https://github.com/JeremyNakano12/NakanoStay.git
```

# Levantar la base de datos
Dirigirse a la carpeta raiz de NakanoStay y ejecutar el comando `docker-compose up -d`

```
cd NakanoStay
docker-compose up -d
```

# Inicializar la aplicación
Usar en el terminal estando en la carpeta raiz NakanoStay `./gradlew bootRun &`

```
./gradlew bootRun &
```

# Probar funcionalidades con Postman
Probar las funcionalidades de la aplicación usando la colección de postman disponible en la carpeta raiz llamada `Integrador.postman_collection.json`
