param(
    [Parameter(Mandatory=$true)][string]$OldModId,
    [Parameter(Mandatory=$true)][string]$NewModId
)

$root = Split-Path -Parent $PSScriptRoot
Get-ChildItem $root -Recurse -File |
    Where-Object { $_.FullName -notmatch '\\.gradle\\|\\build\\|\\.idea\\' } |
    ForEach-Object {
        $content = Get-Content $_.FullName -Raw
        if ($content.Contains($OldModId)) {
            $content.Replace($OldModId, $NewModId) | Set-Content $_.FullName -NoNewline -Encoding UTF8
        }
    }

Write-Host "Text replacement finished. Rename the Java package directory and main class manually." 
