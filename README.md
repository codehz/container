Container
=========

Disclaimer
----------

This program comes with no warranty. You must use this program at your own risk.

Introduction
------------

**This is an Android application, And there is no plan to migrate to other platforms**

Run your program both in the **Container** and System!

The application which running in Container behaves as if it were running on another device, and this is not required to be installed. (Yes, you can run you APK file directly)

WARNING
-------

Do **NOT** use for safety purposes! It is **NOT** a sandbox. Malicious application can still easily break it.

Features
--------

Here is a list of features:

* Easy-to-use and well-designed user interface
* You can run multiple instances of a single application at the same time
* Does not conflict with the same program installed in the operating system
* Each instance has a separate data storage space
* Completely free, and no room for advertising

How to get source code
----------------------

We maintain the source code at GitHub: https://github.com/codehz/container

To get the latest source code, run following command:

```Shell
git clone https://github.com/codehz/container
```

This will create a directory named container in your current directory and source files are stored there.

Dependency
----------

The Core Feature depend on @[asLody](https://github.com/asLody)'s library: [VirtualApp](https://github.com/asLody/VirtualApp).

As well as I made some changes to the form of the patch to stay in the project's root directory(`X.patch`).
If you want to fetch the newest library's source, you can manually apply the patch to the latest branch of its project and copy the lib file back to the root of the project.

License
-------

GPLv3