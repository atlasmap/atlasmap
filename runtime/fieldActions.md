Field Action    | Input Type | Output Type | Parameter(s) (\*=required) | Description
------------ | ---------- | ----------- | ------------ | -----------
AbsoluteValue | Number | Number | n/a | Return the absolute value of a number.
Add | Collection &#124; Array &#124; Map | Number | n/a | Add the numbers in a collection, array, or map's values.
AddDays | Date | Date | days | Add days to a date. The default days is 0.
AddSeconds | Date | Date | seconds | Add seconds to a date. The default seconds is 0.
Append | String | String | string | Append a string to the end of a string. The default is to append nothing.
Average | Collection &#124; Array &#124; Map | Number | n/a | Return the average of the numbers in a collection, array, or map's values.
Camelize | String | String | n/a | Convert a phrase to a camelized string by: <ul><li>Removing whitespace<li>Making the first word lowercase<li>Capitalizing the first letter of each subsequent word</u>
Capitalize | String | String  | n/a | Capitalize the first character of a string.
Ceiling | Number | Number | n/a | Return the whole number ceiling of a number.
Concatenate | Collection &#124; Array &#124; Map | String | delimiter | Concatenate a collection, array, or map's values, separating each entry by the delimiter if supplied.
Contains | Any | Boolean | value | Return true if a field contains the supplied value.
ConvertAreaUnit | Number | Number | fromUnit \*</br>toUnit \*</br> | Convert a number representing an area to another unit. The fromUnit and toUnit parameters can be one of the following: <ul><li>Square Foot<li>Square Meter<li>Square Mile</ul>
ConvertDistanceUnit | Number | Number | fromUnit \*</br>toUnit \*</br> | Convert a number representing a distance to another unit. The fromUnit and toUnit parameters can be one of the following: <ul><li>Foot<li>Inch<li>Meter<li>Mile<li>Yard</ul>
ConvertMassUnit | Number | Number | fromUnit \*</br>toUnit \*</br> | Convert a number representing a mass to another unit. The fromUnit and toUnit parameters can be one of the following: <ul><li>Kilogram<li>Pound</ul>
ConvertVolumeUnit | Number | Number | fromUnit \*</br>toUnit \*</br> | Convert a number representing a volume to another unit. The fromUnit and toUnit parameters can be one of the following: <ul><li>Cubic foot<li>Cubic meter<li>Gallon<li>Liter</ul>
CurrentDate | n/a | Date | n/a | Return the current date.
CurrentDateTime | n/a | Date | n/a | Return the current date/time.
CurrentTime | n/a | Date | n/a | Return the current time.
DayOfWeek | Date | Number | n/a | Return the day of the week for a date, from 1 (Monday) to 7 (Sunday).
DayOfYear | Date | Number | n/a | Return the day of the year for a date, from 1 to 365 (or 366 in a leap year).
Divide | Collection &#124; Array &#124; Map | Number | n/a | Divide each entry in a collection, array, or map's values by its subsequent entry (A "normal" division would only involve 2 entries).
EndsWith | String | Boolean | string | Return true if a string ends with the supplied string (including case).
Equals | Any | Boolean | value | Return true if a field is equal to the supplied value (including case).
FileExtension | String | String | n/a | Retrieve the extension, without the dot ('.'), of a string representing a file name.
Floor | Number | Number | n/a | Return the whole number floor of a number.
Format | Any | String | template \* | Return a string that is the result of substituting a field's value within a template containing placeholders like %s, %d, etc., similar to mechanisms available in programming languages like Java and C.
GenerateUUID | n/a | String | n/a | Create a string representing a random UUID.
IndexOf | String | Number | string | Return the first index, starting at 0, of the supplied string within a string, or -1 if not found.
IsNull | Any | Boolean | n/a | Return true if a field is null.
LastIndexOf | String | Number | string | Return the last index, starting at 0, of the supplied string within a string, or -1 if not found.
Length | Any | Number | n/a | Return the length of the field, or -1 if null. For collections, arrays, and maps, this means the number of entries.
Lowercase | String | String | n/a | Convert a string to lowercase.
Maximum | Collection &#124; Array &#124; Map | Number | n/a | Return the maximum number from the numbers in a collection, array, or map's values.
Minimum | Collection &#124; Array &#124; Map | Number | n/a | Return the minimum number from the numbers in a collection, array, or map's values.
Multiply | Collection &#124; Array &#124; Map | Number | n/a | Multiply the numbers in a collection, array, or map's values.
Normalize | String | String | n/a | Replace consecutive whitespace characters with a single space and trim leading and trailing whitespace from a string.
PadStringLeft | String | String | padCharacter \*</br>padCount \*</br> | Insert the supplied character to the beginning of a string the supplied count times.
PadStringRight | String | String | padCharacter \*</br>padCount \*</br> | Insert the supplied character to the end of a string the supplied count times.
Prepend | String | String | string | Prepend a string to the beginning of a string. The default is to prepend nothing.
ReplaceAll | String | String | match \*</br>newString</br> | Replace all occurrences of the supplied matching string in a string with the supplied newString. The default newString is an empty string.
ReplaceFirst | String | String | match \*</br>newString</br> | Replace this first occurrence of the supplied matching string in a string with the supplied newString. The default newString is an empty string.
Round | Number | Number | n/a | Return the rounded whole number of a number.
SeparateByDash | String | String | n/a | Replace all occurrences of whitespace, colons (:), underscores (\_), plus (+), or equals (=) with a dash (-) in a string.
SeparateByUnderscore | String | String | n/a | Replace all occurrences of whitespace, colon (:), dash (-), plus (+), or equals (=) with an underscores (\_) in a string.
StartsWith | String | Boolean | string | Return true if a string starts with the supplied string (including case).
Substring | String | String | startIndex \*</br>endIndex</br> | Retrieve the segment of a string from the supplied inclusive startIndex to the supplied exclusive endIndex. Both indexes start at zero. The default endIndex is the length of the string.
SubstringAfter | String | String | startIndex \*</br>endIndex</br>match \*</br> | Retrieve the segment of a string after the supplied match string from the supplied inclusive startIndex to the supplied exclusive endIndex. Both indexes start at zero. The default endIndex is the length of the string after the supplied match string.
SubstringBefore | String | String | startIndex \*</br>endIndex</br>match \*</br> | Retrieve the segment of a string before the supplied match string from the supplied inclusive startIndex to the supplied exclusive endIndex. Both indexes start at zero. The default endIndex is the length of the string before the supplied match string.
Subtract | Collection &#124; Array &#124; Map | Number | n/a | Subtract each entry in a collection, array, or map's values from its previous entry (A "normal" subtraction would only involve 2 entries).
Trim | String | String | n/a | Trim leading and trailing whitespace from a string.
TrimLeft | String | String | n/a | Trim leading whitespace from a string.
TrimRight | String | String | n/a | Trim trailing whitespace from a string.
Uppercase | String | String | n/a | Convert a string to uppercase.
