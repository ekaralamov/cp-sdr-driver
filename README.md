RTL-SDR CP Driver is an Android application that provides an interface for other applications to
access RTL2832-based USB software-defined radio receivers.

Overview
========

RTL-SDR CP Driver utilises the
[rtl-sdr](https://osmocom.org/projects/rtl-sdr/wiki/Rtl-sdr#Software) codebase and is meant to be
kept in sync with the developments there. The provided interface mirrors the functionality of
rtl_tcp in an Android way. Instead of via a TCP socket, the communication is carried out through
file descriptors returned by a `ContentProvider`.

Since some potentially sensitive information could be captured through the SDR receivers, like
indications of the device location, the RTL-SDR CP Driver implements permission control similar to
that of the Android framework. Prior to accessing receivers, client applications have to ask the
user for permission to access the driver by starting the driver's permission flow via
`startActivityForResult`. Once the user grants access, their answer is remembered and they are
not prompted again. The user has the ability to later revoke the permission from the driver's UI,
accessible via the Android launcher.

Supported Devices
=================

RTL-SDR CP Driver supports the devices that librtlsdr supports.
[Here](permissions/shell/src/main/res/xml/device_filter.xml)'s a list of them.

Client Interface
================

Client application interaction with RTL-SDR CP Driver comprises of two steps:

* requesting permission by starting the driver's permission flow via `startActivityForResult`
* opening commands and data channels via the driver's `ContentProvider`.

These operations are facilitated by the driver's client library, available for clients' Gradle
projects as:

```groovy
implementation 'sdr.driver.cp:client:1.0.0'
```

Requesting Permission
---------------------

Client applications should start the driver's permission flow prior to every connection to a
receiver. This can be accomplished with:

```kotlin
startActivityForResult(TunerAccessClient.intent(receiverUsbDevice), requestCode)
```

The flow has two concerns: determining whether the user allows the client application to access
the driver, and securing the driver's access to the USB device of interest. The user might be
prompted for both, one or none of these, depending on prior resolutions. `RESULT_OK` means both
permissions have been granted and the client is good-to-go to access the requested receiver. For
other possible results see
[TunerAccessClient](permissions/shell/clientshared/src/main/java/sdr/driver/cp/permissions/TunerAccessClient.kt).

Accessing Receivers
-------------------

To access a receiver, clients need to open two streams by using `ContentResolver`. One of the
streams is for writing the commands and the other is for reading the data. These two streams
combined function exactly as rtl_tcp does. The list of commands can be found in
[rtl_tcp.c](opening/operations/rtl-sdr/src/rtl_tcp.c).

Opening the commands stream (i.e. mode `"w"`):

```kotlin
contentResolver.openFileDescriptor(TunerContentUri.build(receiverUsbDevice, context), "w")
```

Opening the data stream (i.e. mode `"r"`):

```kotlin
contentResolver.openFileDescriptor(TunerContentUri.build(receiverUsbDevice, context), "r")
```

The streams can be opened in any order. For any particular receiver, only one stream of each type
can be opened at a time, with further opening requests blocking until the previous stream is
closed. If the receiver is being used by another application, the `openFileDescriptor` call does not
block and fails immediately by returning `null`.

Building
========

In the building of the project there is a little quirk. There are two modules containing native
code, `opening-operations` and `opening-operations-libusb`, the first being dependent on the second.
At the time of their development the Android Gradle Plugin did not support native exports/imports
between modules, so custom plugins were developed for that purpose. They do the job well, except
for one issue that could not be overcome, the generation of the `.cxx` directory of
`opening-operations` needs to happen after `libusb` has been built, but in a clean source tree it
does not and the build fails. The workaround is:

1. start the build

   -> `libusb` builds

2. wait for the building of `opening-operations` to fail

3. delete the `.cxx` directory of `opening-operations`

4. build again

   -> the `.cxx` directory is now generated correctly and the building succeeds.

This is needed only when the source tree is clean. The subsequent incremental builds work normally.

License
=======

RTL-SDR CP Driver, except for the code shared with the client library, is licensed under the
[GNU General Public License v3.0](Licenses/GPL-3.txt) or any later version.

The client library and the code shared between it and the driver are licensed under the
[Apache License 2.0](Licenses/Apache-2.0.txt).
