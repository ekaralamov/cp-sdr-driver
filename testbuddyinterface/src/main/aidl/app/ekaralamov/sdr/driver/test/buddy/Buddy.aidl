package app.ekaralamov.sdr.driver.test.buddy;

// Declare any non-default types here with import statements

interface Buddy {

    int requestAccess(in String deviceName);

    int waitForAccess(int requestKey);
}
