
# react-native-epub-parser

## PRs are welcomed!

## Getting started

`$ npm install react-native-epub-parser --save`

or if you prefer yarn

`$ yarn add react-native-epub-parser`

### Mostly automatic installation

`$ react-native link react-native-epub-parser`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-epub-parser` and add `RNEpubParser.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNEpubParser.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNEpubParserPackage;` to the imports at the top of the file
  - Add `new RNEpubParserPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-epub-parser'
  	project(':react-native-epub-parser').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-epub-parser/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-epub-parser')
  	```


## Usage
```javascript
import RNEpubParser from 'react-native-epub-parser';

// TODO: What to do with the module?
RNEpubParser;
```
  