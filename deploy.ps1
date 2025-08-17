# Uso: .\deploy.ps1 [usuario-dockerhub] [tag]

param(
    [string]$DockerUser = "tu-usuario",
    [string]$Tag = "latest"
)

$ImageName = "$DockerUser/nakanostay-backend:$Tag"

Write-Host "Iniciando deployment de NakanoStay Backend..." -ForegroundColor Green
Write-Host "Imagen: $ImageName" -ForegroundColor Yellow

try {

    # 1. Verificar que el JAR existe
    $jarFiles = Get-ChildItem -Path "build\libs\*.jar" -ErrorAction SilentlyContinue
    if (-not $jarFiles) {
        throw "Error: JAR no encontrado en build\libs\"
    }

    Write-Host "JAR construido exitosamente" -ForegroundColor Green

    # 2. Construir imagen Docker
    Write-Host "Construyendo imagen Docker..." -ForegroundColor Cyan
    docker build -f Dockerfile.prod -t $ImageName .
    if ($LASTEXITCODE -ne 0) { throw "Error construyendo imagen Docker" }

    # 3. Push a Docker Hub
    Write-Host "Subiendo imagen a Docker Hub..." -ForegroundColor Cyan
    docker push $ImageName
    if ($LASTEXITCODE -ne 0) { throw "Error subiendo imagen" }

    Write-Host "Imagen subida exitosamente: $ImageName" -ForegroundColor Green
    Write-Host ""
    Write-Host "Próximos pasos en tu servidor EC2:" -ForegroundColor Yellow
    Write-Host "1. docker pull $ImageName"
    Write-Host "2. Actualizar docker-compose.prod.yml con la imagen: $ImageName"
    Write-Host "3. docker-compose -f docker-compose.prod.yml up -d"
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
}