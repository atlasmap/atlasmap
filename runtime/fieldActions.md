Field Action    | Input Type | Output Type | Parameter(s) (\*=required) | Description
------------ | ---------- | ----------- | ------------ | -----------
Camelize | String | String | n/a | Convert a phrase to a camelized string by: <ul><li>Removing whitespace<li>Making the first word lowercase<li>Capitalizing the first letter of each subsequent word</u>
Capitalize | String | String  | n/a | Capitalize the first character of a string.
ConvertAreaUnit | Number | Number | fromUnit \*</br>toUnit \*</br> | Convert a number representing an area to another unit. The fromUnit and toUnit parameters can be one of the following: <ul><li>Square Foot<li>Square Meter<li>Square Mile</ul>
ConvertDistanceUnit | Number | Number | fromUnit \*</br>toUnit \*</br> | Convert a number representing a distance to another unit. The fromUnit and toUnit parameters can be one of the following: <ul><li>Foot<li>Inch<li>Meter<li>Mile<li>Yard</ul>
ConvertMassUnit | Number | Number | fromUnit \*</br>toUnit \*</br> | Convert a number representing a mass to another unit. The fromUnit and toUnit parameters can be one of the following: <ul><li>Kilogram<li>Pound</ul>
ConvertVolumeUnit | Number | Number | fromUnit \*</br>toUnit \*</br> | Convert a number representing a volume to another unit. The fromUnit and toUnit parameters can be one of the following: <ul><li>Cubic foot<li>Cubic meter<li>Gallon<li>Liter</ul>
CurrentDate | n/a | String | dateFormat | Create a string representing the current date in the supplied format. The default format is "yyyy-MM-dd".
CurrentDateTime | n/a | String | dateFormat | Create a string representing the current date and time in the supplied format. The default format is "yyyy-MM-dd'T'HH:mm'Z'".
CurrentTime | n/a | String | dateFormat | Create a string representing the current time in the supplied format. The default format is "HH:mm:ss".
GenerateUUID | n/a | String | n/a | Create a string representing a random UUID.
Lowercase | String | String | n/a | Convert a string to lowercase.
PadStringLeft | String | String | padCharacter \*</br>padCount \*</br> | Insert the supplied character to the beginning of a string the supplied count times.
PadStringRight | String | String | padCharacter \*</br>padCount \*</br> | Insert the supplied character to the end of a string the supplied count times.
Replace | String | String | oldString \*</br>newString</br> | Replace all occurrences of the supplied oldString in a string with the supplied newString. The default newString is an empty string.
SeparateByDash | String | String | n/a | Replace all occurrences of whitespace, colons (:), underscores (\_), plus (+), or equals (=) with a dash (-) in a string.
SeparateByUnderscore | String | String | n/a | Replace all occurrences of whitespace, colon (:), dash (-), plus (+), or equals (=) with an underscores (\_) in a string.
StringLength | String | Integer | n/a | Return the length of a string.
Trim | String | String | n/a | Trim leading and trailing whitespace from a string.
TrimLeft | String | String | n/a | Trim leading whitespace from a string.
TrimRight | String | String | n/a | Trim trailing whitespace from a string.
Uppercase | String | String | n/a | Convert a string to uppercase.
Substring | String | String | startIndex \*</br>endIndex</br> | Retrieve the segment of a string from the supplied inclusive startIndex to the supplied exclusive endIndex. Both indexes start at zero. The default endIndex is the length of the string.
SubstringAfter | String | String | startIndex \*</br>endIndex</br>match \*</br> | Retrieve the segment of a string after the supplied match string from the supplied inclusive startIndex to the supplied exclusive endIndex. Both indexes start at zero. The default endIndex is the length of the string after the supplied match string.
SubstringBefore | String | String | startIndex \*</br>endIndex</br>match \*</br> | Retrieve the segment of a string before the supplied match string from the supplied inclusive startIndex to the supplied exclusive endIndex. Both indexes start at zero. The default endIndex is the length of the string before the supplied match string.
SumUp | Complex | Number | numberType | Add the numbers in a collection, array, or map. The collection/array must only contain numbers; For a map, the values must only be numbers. The result will be of the supplied numberType, if provided, or otherwise the type of the first item in the collection/array/values.


Test change
