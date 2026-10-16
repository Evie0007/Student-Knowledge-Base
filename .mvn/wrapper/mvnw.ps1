# Downloads the Maven version named in maven-wrapper.properties (once) and prints its install folder.
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$props = Join-Path $PSScriptRoot 'maven-wrapper.properties'
$url = ((Get-Content $props | Where-Object { $_ -match '^\s*distributionUrl\s*=' }) -split '=', 2)[1].Trim()
$name = [IO.Path]::GetFileNameWithoutExtension($url)            # apache-maven-3.9.11-bin
$base = Join-Path $env:USERPROFILE ".m2\wrapper\dists\$name"
$mavenHome = Join-Path $base ($name -replace '-bin$', '')         # apache-maven-3.9.11

if (-not (Test-Path (Join-Path $mavenHome 'bin\mvn.cmd'))) {
    [Console]::Error.WriteLine("Downloading Maven from $url ...")
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    New-Item -ItemType Directory -Force $base | Out-Null
    $zip = Join-Path $base "$name.zip"
    $sumFile = "$zip.sha512"
    Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
    Invoke-WebRequest -Uri "$url.sha512" -OutFile $sumFile -UseBasicParsing

    $expected = ((Get-Content $sumFile -Raw).Trim() -split '\s+')[0]
    $actual = (Get-FileHash $zip -Algorithm SHA512).Hash
    if ($actual -ne $expected.ToUpper()) {
        Remove-Item $zip, $sumFile
        throw "Checksum mismatch for $url"
    }

    Expand-Archive $zip -DestinationPath $base -Force
    Remove-Item $zip, $sumFile
    [Console]::Error.WriteLine("Maven installed to $mavenHome")
}

Write-Output $mavenHome
