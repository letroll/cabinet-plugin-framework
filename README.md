# Cabinet Plugin Framework

These docs will be updated later.

[ ![Download](https://api.bintray.com/packages/drummer-aidan/maven/cabinet-plugin-framework/images/download.svg) ](https://bintray.com/drummer-aidan/maven/cabinet-plugin-framework/_latestVersion)

# Services

The heart of a Cabinet plugin is a service. When Cabinet searches for installed plugins, it searches
for services that receive an intent filter. Your plugin needs a class that extends `PluginService`,
it must override all the required methods, and should have an intent filter that receives the action
`com.afollestad.cabinet.plugins.SERVICE` (in your AndroidManifest.xml file).

Installed plugins are displayed at the top of the navigation drawer in Cabinet as unremovable bookmarks. 
When the user navigates to the plugin, Cabinet will start the plugin service, tell it to connect, and 
request a file listing for the root path.

# Authenticators

During connection, if your service says that it needs to authenticate, Cabinet will start your plugin's
authenticator Activity. This Activity must extend `PluginAuthenticator`. The authenticator can store
information however it wants, but when it's done, it calls `notifyAuthenticated()` and finishes. At this point,
the service will be notified and it will attempt to connect again.