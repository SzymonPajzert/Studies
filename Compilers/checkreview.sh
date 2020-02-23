for file in $(find . -name '*.scala'); do
	header=$(head -1 $file)

	if [[ $header == //* ]]; then
		echo "${header##*: } $file" 
	else
		echo "0000.00.00 $file"
	fi
done | sort
