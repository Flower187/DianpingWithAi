param(
    [string]$NginxRoot = 'F:\develop\nginx\hmdp\nginx-1.18.0\nginx-1.18.0'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$NginxRoot = (Resolve-Path -LiteralPath $NginxRoot).Path
$nginxExecutable = Join-Path $NginxRoot 'nginx.exe'
$webRoot = Join-Path $NginxRoot 'html\hmdp'
if (-not (Test-Path -LiteralPath (Join-Path $webRoot 'index.html'))) {
    throw '未找到点评前端 index.html，请检查 NginxRoot 参数。'
}

$files = @(
    @{ Source = 'deploy\nginx.conf'; Target = 'conf\nginx.conf' },
    @{ Source = 'frontend\js\common.js'; Target = 'html\hmdp\js\common.js' },
    @{ Source = 'frontend\js\agent-widget.js'; Target = 'html\hmdp\js\agent-widget.js' },
    @{ Source = 'frontend\css\agent-widget.css'; Target = 'html\hmdp\css\agent-widget.css' }
)
$backupRoot = Join-Path $PSScriptRoot ('backups\' + [Guid]::NewGuid().ToString('N'))
foreach ($page in Get-ChildItem (Join-Path $projectRoot 'frontend') -Filter '*.html') {
    $files += @{ Source = ('frontend\' + $page.Name); Target = ('html\hmdp\' + $page.Name) }
}
New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null
$installed = @()
try {
    foreach ($file in $files) {
        $target = Join-Path $NginxRoot $file.Target
        $backup = Join-Path $backupRoot $file.Target
        $existed = Test-Path -LiteralPath $target
        if ($existed) {
            New-Item -ItemType Directory -Path (Split-Path -Parent $backup) -Force | Out-Null
            Copy-Item -LiteralPath $target -Destination $backup
        }
        $installed += @{ Target = $target; Backup = $backup; Existed = $existed }
        Copy-Item -LiteralPath (Join-Path $projectRoot $file.Source) -Destination $target -Force
    }
    & $nginxExecutable -p ($NginxRoot + '\') -t
    if ($LASTEXITCODE -ne 0) { throw 'Nginx 配置校验失败。' }

    $running = Get-Process -Name nginx -ErrorAction SilentlyContinue |
        Where-Object { $_.Path -eq $nginxExecutable }
    if ($running) {
        & $nginxExecutable -p ($NginxRoot + '\') -s reload
        if ($LASTEXITCODE -ne 0) { throw 'Nginx 重载失败。' }
    } else {
        Start-Process -FilePath $nginxExecutable -WorkingDirectory $NginxRoot -WindowStyle Hidden
    }
    Write-Output "已安装 AI 入口。备份位置 $backupRoot"
    Write-Output '访问 http://localhost:8080/，刷新后点击右下角 AI 助手。'
} catch {
    foreach ($file in $installed) {
        if ($file.Existed) {
            Copy-Item -LiteralPath $file.Backup -Destination $file.Target -Force
        } else {
            Remove-Item -LiteralPath $file.Target -ErrorAction SilentlyContinue
        }
    }
    throw
}
