Add-Type -AssemblyName System.Drawing

function New-Color([string]$hex, [int]$alpha = 255) {
    $clean = $hex.TrimStart("#")
    return [System.Drawing.Color]::FromArgb(
        $alpha,
        [Convert]::ToInt32($clean.Substring(0, 2), 16),
        [Convert]::ToInt32($clean.Substring(2, 2), 16),
        [Convert]::ToInt32($clean.Substring(4, 2), 16)
    )
}

function New-Canvas([int]$width, [int]$height) {
    $bitmap = New-Object System.Drawing.Bitmap $width, $height
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $graphics.Clear([System.Drawing.Color]::Transparent)
    return @{
        Bitmap = $bitmap
        Graphics = $graphics
    }
}

function New-MihrabPath([float]$x, [float]$y, [float]$width, [float]$height) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $archHeight = $height * 0.45
    $bottomRadius = $width * 0.08

    $path.StartFigure()
    $path.AddArc($x, $y, $width, $archHeight * 2, 180, 180)
    $path.AddLine($x + $width, $y + $archHeight, $x + $width, $y + $height - $bottomRadius)
    $path.AddArc($x + $width - ($bottomRadius * 2), $y + $height - ($bottomRadius * 2), $bottomRadius * 2, $bottomRadius * 2, 0, 90)
    $path.AddLine($x + $width - $bottomRadius, $y + $height, $x + $bottomRadius, $y + $height)
    $path.AddArc($x, $y + $height - ($bottomRadius * 2), $bottomRadius * 2, $bottomRadius * 2, 90, 90)
    $path.AddLine($x, $y + $height - $bottomRadius, $x, $y + $archHeight)
    $path.CloseFigure()
    return $path
}

function Draw-Book([System.Drawing.Graphics]$graphics, [float]$centerX, [float]$centerY, [float]$width) {
    $height = $width * 0.46
    $pageFill = New-Color "#F5FBF7"
    $pageStroke = New-Color "#7CA88F"
    $crease = New-Color "#0F5238"

    $leftPoints = @(
        [System.Drawing.PointF]::new($centerX - ($width * 0.42), $centerY - ($height * 0.04)),
        [System.Drawing.PointF]::new($centerX - ($width * 0.17), $centerY - ($height * 0.22)),
        [System.Drawing.PointF]::new($centerX - ($width * 0.02), $centerY - ($height * 0.08)),
        [System.Drawing.PointF]::new($centerX - ($width * 0.02), $centerY + ($height * 0.34)),
        [System.Drawing.PointF]::new($centerX - ($width * 0.17), $centerY + ($height * 0.18)),
        [System.Drawing.PointF]::new($centerX - ($width * 0.38), $centerY + ($height * 0.26))
    )
    $rightPoints = @(
        [System.Drawing.PointF]::new($centerX + ($width * 0.42), $centerY - ($height * 0.04)),
        [System.Drawing.PointF]::new($centerX + ($width * 0.17), $centerY - ($height * 0.22)),
        [System.Drawing.PointF]::new($centerX + ($width * 0.02), $centerY - ($height * 0.08)),
        [System.Drawing.PointF]::new($centerX + ($width * 0.02), $centerY + ($height * 0.34)),
        [System.Drawing.PointF]::new($centerX + ($width * 0.17), $centerY + ($height * 0.18)),
        [System.Drawing.PointF]::new($centerX + ($width * 0.38), $centerY + ($height * 0.26))
    )

    $pageBrush = New-Object System.Drawing.SolidBrush $pageFill
    $pagePen = New-Object System.Drawing.Pen $pageStroke, ($width * 0.025)
    $pagePen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round

    $graphics.FillClosedCurve($pageBrush, $leftPoints)
    $graphics.FillClosedCurve($pageBrush, $rightPoints)
    $graphics.DrawClosedCurve($pagePen, $leftPoints)
    $graphics.DrawClosedCurve($pagePen, $rightPoints)

    $creasePen = New-Object System.Drawing.Pen $crease, ($width * 0.028)
    $creasePen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $creasePen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $graphics.DrawLine(
        $creasePen,
        $centerX,
        $centerY - ($height * 0.12),
        $centerX,
        $centerY + ($height * 0.28)
    )

    $pageBrush.Dispose()
    $pagePen.Dispose()
    $creasePen.Dispose()
}

function Draw-QiblaNeedle([System.Drawing.Graphics]$graphics, [float]$centerX, [float]$centerY, [float]$size) {
    $gold = New-Color "#D6B66A"
    $emerald = New-Color "#0F5238"
    $needle = @(
        [System.Drawing.PointF]::new($centerX, $centerY - ($size * 0.54)),
        [System.Drawing.PointF]::new($centerX + ($size * 0.22), $centerY),
        [System.Drawing.PointF]::new($centerX, $centerY + ($size * 0.30)),
        [System.Drawing.PointF]::new($centerX - ($size * 0.22), $centerY)
    )
    $needleBrush = New-Object System.Drawing.SolidBrush $gold
    $needlePen = New-Object System.Drawing.Pen $emerald, ($size * 0.05)
    $needlePen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
    $graphics.FillPolygon($needleBrush, $needle)
    $graphics.DrawPolygon($needlePen, $needle)

    $dotBrush = New-Object System.Drawing.SolidBrush $emerald
    $dotSize = $size * 0.14
    $graphics.FillEllipse($dotBrush, $centerX - ($dotSize / 2), $centerY - ($dotSize / 2), $dotSize, $dotSize)

    $needleBrush.Dispose()
    $needlePen.Dispose()
    $dotBrush.Dispose()
}

function Draw-BrandMark([System.Drawing.Graphics]$graphics, [float]$x, [float]$y, [float]$size) {
    $haloColor = New-Color "#E6F2EC"
    $outerStroke = New-Color "#0F5238"
    $innerStroke = New-Color "#8CB79A"

    $haloBrush = New-Object System.Drawing.SolidBrush $haloColor
    $graphics.FillEllipse($haloBrush, $x + ($size * 0.08), $y + ($size * 0.08), $size * 0.84, $size * 0.84)

    $outerPath = New-MihrabPath ($x + ($size * 0.18)) ($y + ($size * 0.10)) ($size * 0.64) ($size * 0.72)
    $innerPath = New-MihrabPath ($x + ($size * 0.26)) ($y + ($size * 0.20)) ($size * 0.48) ($size * 0.52)

    $outerPen = New-Object System.Drawing.Pen $outerStroke, ($size * 0.055)
    $innerPen = New-Object System.Drawing.Pen $innerStroke, ($size * 0.022)
    $outerPen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
    $innerPen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
    $graphics.DrawPath($outerPen, $outerPath)
    $graphics.DrawPath($innerPen, $innerPath)

    Draw-QiblaNeedle $graphics ($x + ($size * 0.5)) ($y + ($size * 0.33)) ($size * 0.19)
    Draw-Book $graphics ($x + ($size * 0.5)) ($y + ($size * 0.60)) ($size * 0.50)

    $haloBrush.Dispose()
    $outerPen.Dispose()
    $innerPen.Dispose()
    $outerPath.Dispose()
    $innerPath.Dispose()
}

function Save-Png($canvas, [string]$path) {
    $directory = Split-Path -Parent $path
    if (-not (Test-Path $directory)) {
        New-Item -ItemType Directory -Path $directory | Out-Null
    }
    $canvas.Bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $canvas.Graphics.Dispose()
    $canvas.Bitmap.Dispose()
}

$outputDir = Join-Path $PSScriptRoot "..\\app\\src\\main\\res\\drawable-nodpi"

$markCanvas = New-Canvas 1024 1024
Draw-BrandMark $markCanvas.Graphics 0 0 1024
Save-Png $markCanvas (Join-Path $outputDir "sajda_logo_mark.png")

$fullCanvas = New-Canvas 1200 1400
Draw-BrandMark $fullCanvas.Graphics 180 110 840

$headlineBrush = New-Object System.Drawing.SolidBrush (New-Color "#0F5238")
$subtleBrush = New-Object System.Drawing.SolidBrush (New-Color "#6D7D74")
$headlineFont = New-Object System.Drawing.Font "Segoe UI Semibold", 112, ([System.Drawing.FontStyle]::Regular), ([System.Drawing.GraphicsUnit]::Pixel)
$subtitleFont = New-Object System.Drawing.Font "Segoe UI", 38, ([System.Drawing.FontStyle]::Regular), ([System.Drawing.GraphicsUnit]::Pixel)
$centerFormat = New-Object System.Drawing.StringFormat
$centerFormat.Alignment = [System.Drawing.StringAlignment]::Center
$centerFormat.LineAlignment = [System.Drawing.StringAlignment]::Center

$headlineRect = New-Object System.Drawing.RectangleF 150, 970, 900, 120
$subtitleRect = New-Object System.Drawing.RectangleF 150, 1090, 900, 70

$fullCanvas.Graphics.DrawString("Sajda App", $headlineFont, $headlineBrush, $headlineRect, $centerFormat)
$fullCanvas.Graphics.DrawString("Qur'an and prayer companion", $subtitleFont, $subtleBrush, $subtitleRect, $centerFormat)

$headlineBrush.Dispose()
$subtleBrush.Dispose()
$headlineFont.Dispose()
$subtitleFont.Dispose()
$centerFormat.Dispose()

Save-Png $fullCanvas (Join-Path $outputDir "sajda_logo_full.png")
