# searchEngine

Thank you for taking the time to review my work. Here are some assumptions that were made / decisions that were taken:

1. Search function supports "exact match" per keyword, and does not support "contains". Meaning, if the word "cat" appears in a page, you will need to search for "cat" (in all uppercase or lowercase formats) - searching for "ca" won't work.
2. The corputs file is opened using FileReader, so the file either needs to be in the same folder as searchEngine.java (in which case, "filename.txt" is enoguh) or the path to the file should be provided in the appropriate way.

The search engine works as follows:
* Compile searchEngine.java using javac SearchEngine.java 
* Run searchEngine using java searchEngine
* When asked, enter the name of the file and hit Enter
** Test
