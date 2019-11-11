
# Sieben GT - Web View  
Android Application Web View built for Sieben Brazil.
  
This application presents the content of [Doc System Cloud Doc](http://docsystem5.clouddoc.com.br/SimplePortal/Pages/Login.html)  
in a native way for Android operational systems.

# Build  
You can use [Android Studio](https://developer.android.com/studio) IDE to build the application or you can build it manually through gradle script   
  
## Using Android Studio  
  
In the project's root folder, open Android Studio, select **"Open an existing Android Studio project"** and click on **"OK".** All gradle tasks should start running and you can run the app through **"Run -> Run app"** menu. 
 
## Manually

In the project's root folder, run in the shell

```bash
$ .gradlew build
```
You should see the **.apk** file in `app/build/debug` or `app/build/release` (depending on the build variant you have chosen)

# Versions
You can see all the released versions in [Releases](releases) tab