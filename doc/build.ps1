
function mkdoc([string]$doc) {
	$CTXPATH="C:\Software\ConTeXt"
	$env:CONTEXT=$CTXPATH
	$env:SETUPTEX="done"
	$env:TEXMFOS="$CTXPATH\tex\texmf-win64"
	$env:PATH="$env:PATH;$env:TEXMFOS\bin"

	pandoc -f markdown -t context -s "$doc.md" -o "$doc.tex"

	& "$CTXPATH\tex\texmf-win64\bin\context.exe" "$doc.tex"
	echo

}
