/*
    takes an arraylist of files to pass to git add 
*/
void add(ArrayList files) {
    println "git add ${files.join(" ")}"
}

/*
    takes a string commit message to pass to git commit 
*/
void commit(String message) {
    println "git commit -m ${message}" 
}

/*
    performs the git push
*/
void push() {
    println "git push" 
}
