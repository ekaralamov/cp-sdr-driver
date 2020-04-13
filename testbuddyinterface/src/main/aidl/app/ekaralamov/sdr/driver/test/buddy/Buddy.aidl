package app.ekaralamov.sdr.driver.test.buddy;

// Declare any non-default types here with import statements

interface Buddy {

    int requestAccess(in Uri uri);

    boolean waitForAccess(int requestKey);
}
