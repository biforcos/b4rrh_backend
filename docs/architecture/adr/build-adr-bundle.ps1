$OutFile = "ADR_BUNDLE.md"
$TempFile = [System.IO.Path]::GetTempFileName()

# Obtener los markdown, excluyendo el bundle
$files = Get-ChildItem -File -Filter *.md |
    Where-Object { $_.Name -ne $OutFile } |
    Sort-Object Name

# Cabecera
@"
# ADR Bundle

> Fichero generado automáticamente. No editar a mano.
> Fecha de generación: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

---

## Índice

"@ | Set-Content -Path $TempFile -Encoding UTF8

# Índice
foreach ($file in $files) {
    $base = $file.Name
    $anchor = ($base.ToLower() -replace '[^a-z0-9]', '-')
    "- [$base](#file-$anchor)" | Add-Content -Path $TempFile -Encoding UTF8
}

@"

---

"@ | Add-Content -Path $TempFile -Encoding UTF8

# Concatenación
foreach ($file in $files) {
    $base = $file.Name
    $anchor = ($base.ToLower() -replace '[^a-z0-9]', '-')
    @"

---

# FILE: $base
<a name="file-$anchor"></a>

<!-- BEGIN FILE: $base -->

"@ | Add-Content -Path $TempFile -Encoding UTF8

    Get-Content -Path $file.FullName -Raw | Add-Content -Path $TempFile -Encoding UTF8

    @"

<!-- END FILE: $base -->

"@ | Add-Content -Path $TempFile -Encoding UTF8
}

Move-Item -Path $TempFile -Destination $OutFile -Force

Write-Host "Generado: $OutFile"