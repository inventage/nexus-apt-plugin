Nexus APT Plugin
================

this plugin generates a Packages.gz for each nexus repository and allows the repository to be 
listed in a debian /etc/apt/sources.list file so that it can be used by aptitude/apt-get/ubuntu 
software center.

Installation
============

The 'Downloads' section of this project contains the latest builds. Please download the latest 
nexus-apt-plugin-N.N-bundle.zip and unzip it into the sonatype-work/nexus/plugin-repository/
and restart nexus.

> to be sure that the index is regenerated (the plugin adds attributes to index) it could be 
neccessary to delete the index files under sonatype-work/nexus/indexer

All repositories now contain a Packages.gz that lists all debian packages the indexer was able 
to find.

Compatibility
-------------

| Nexus Version      | Plugin Version |
| ------------------ | -------------- |
| 3.x and greater    | Not compatible (see https://github.com/sonatype-nexus-community/nexus-repository-apt) |
| 2.11.x and greater | 1.1.2          |
| 2.8.x and greater  | 1.0.2          |
| 2.7.x              | 0.6            |

The plugins might be compatible with earlier Nexus versions, but are not tested.


Configuring Release Signing
---------------------------

APT will noisely complain when updating from a repository with an unsigned
Packages.gz. 

To configure signing, you need a PGP key. You can generate one by running:

    > gpg --gen-key
   
Choose "RSA and RSA (default)", and a key size of 2048. Larger key sizes are
not supported by this plugin.

Once the key is generated, you can run `gpg --list-keys` to find the 
ID of the generated key:

    > gpg --list-keys
    /home/nexus/.gnupg/pubring.gpg
    ------------------------------
    pub   2048R/4FE5A5F6 2016-10-08 [expires: 2017-10-08]
    uid                  Alexander Bertram <alex@bedatadriven.com>
    sub   2048R/13E1BD68 2016-10-08 [expires: 2017-10-08]

The Key ID can be found following the '2048R/'. For example, the ID above
is `4FE5A5F6`.

You may have to restart Nexus before continuing.

To configure the plugin to use this key, navigate to "Administration", 
and then "Capabilities" in the Nexus web interface.

Click the "New" button, choose the type "APT: Configuration", and then
reply the requested settings, for example:

| Setting                  | Plugin Version                  |
| ------------------------ | ------------------------------- |
| Secure keyring location  | /home/nexus/.gnupg/secring.gpg  |
| Key ID                   | 4FE5A5F6                        |
| Passphrase for the key   | xxxxxxxxx                       |


In order for APT to verify the signature, you must share the public 
key with your users. One way to do this is to publish your key to
a key server. For example:

    gpg --keyserver keyserver.ubuntu.com --send-key 4FE5A5F6


Debian Packages from a Maven Build Process
------------------------------------------
https://github.com/sannies/blogger-java-deb is a small example on how to create debs in a 
Maven process and works together well with this plugin.


Pitfall
-------

The indexer cannot find packages when there is a main artifact with the same name:
If the artifacts are named like:

-  nexus-apt-plugin-0.5.jar
-  nexus-apt-plugin-0.5.deb

The indexer won't index the debian package. In order to make the indexer index the debian 
package it needs a classifier:

-  nexus-apt-plugin-0.5.jar
-  nexus-apt-plugin-0.5-all.deb

This is fine.

Known Bug
---------

When maven uploads an artifact to Nexus, it uploads 2 files: the deb file
and the pom. Nexus then indexes the artifact, but it does this for the 2 files simultaneously.
Depending on which file is uploaded last (seems to be random), the debian package information
may or may not be in the index and therefore may or may not be in the Packages/Packages.gz
files.

To fix the index contents, start an 'Update Index' action on the repository.

This is an issue with the indexer in Nexus 2.x.y. Sonatype is planning to use a new indexer
technology in their Nexus 3.0 release.

Adding a repository to sources.list
===================================

First add your PGP key if you have configured signing. If you published your key
to a key server as described above, you can run:

    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com --recv-keys 4FE5A5F6

Then add the line `deb http://repository.yourcompany.com/content/repositories/releases/ ./`
to your `/etc/apt/sources.list`. Type `apt-get update` and all debian packages in the repository
can now be installed via `apt-get install`.

Author
======

This plugin was created by https://github.com/sannies and is now maintained by https://github.com/inventage.
