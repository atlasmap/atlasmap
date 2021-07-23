---
name: Bug report
about: Create a bug report to help us improve
title: "[BUG] "
labels: cat/bug
assignees: ''

---

**Describe the bug**
A clear and concise description of what the bug is. The more info is provided, the more likely we can fix it earlier.

**Attach .adm file and source document examples**
The .adm archive file you can export from AtlasMap UI contains all metadata to reproduce your mapping. Also don't forget to provide source document examples to reproduce the runtime behavior exactly what you see.
 
**Attach debug logs**
If it's a design time issue, attach a backend log with debug enabled
`java -Dlogging.level.io.atlasmap=debug -jar atlasmap-standalone.jar`
or for runtime issue, set the log level for `io.atlasmap` package to debug in your log4j config
 
**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behavior**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Environment (please complete the following information):**
 - OS: [e.g. iOS]
 - Browser [e.g. chrome, safari]
 - Version [e.g. 22]

**Additional context**
Add any other context about the problem here.
