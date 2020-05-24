package app.ekaralamov.sdr.driver.test.buddy;

// Declare any non-default types here with import statements

interface Buddy {

    int requestAccess(in UsbDevice device);

    int waitForResult(int requestKey);

    ParcelFileDescriptor openCommandsChannel(in UsbDevice device);

    ParcelFileDescriptor openDataChannel(in UsbDevice device);
}
