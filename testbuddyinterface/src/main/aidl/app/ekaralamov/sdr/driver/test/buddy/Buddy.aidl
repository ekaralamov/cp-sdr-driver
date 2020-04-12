package app.ekaralamov.sdr.driver.test.buddy;

// Declare any non-default types here with import statements

interface Buddy {
    oneway void requestAccess(in UsbDevice device);
}
