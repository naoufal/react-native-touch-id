# React Native Touch ID

[![react-native version](https://img.shields.io/badge/react--native-0.40-blue.svg?style=flat-square)](http://facebook.github.io/react-native/releases/0.40)
[![npm version](https://img.shields.io/npm/v/react-native-touch-id.svg?style=flat-square)](https://www.npmjs.com/package/react-native-touch-id)
[![npm downloads](https://img.shields.io/npm/dm/react-native-touch-id.svg?style=flat-square)](https://www.npmjs.com/package/react-native-touch-id)
[![Code Climate](https://img.shields.io/codeclimate/github/naoufal/react-native-touch-id.svg?style=flat-square)](https://codeclimate.com/github/naoufal/react-native-touch-id)

React Native Touch ID is a [React Native](http://facebook.github.io/react-native/) library for authenticating users with Touch ID.

![react-native-touch-id](https://cloud.githubusercontent.com/assets/1627824/7975919/2c69a776-0a42-11e5-9773-3ea1c7dd79f3.gif)

## Documentation
- [Install](https://github.com/naoufal/react-native-touch-id#install)
- [Usage](https://github.com/naoufal/react-native-touch-id#usage)
- [Example](https://github.com/naoufal/react-native-touch-id#example)
- [Fallback](https://github.com/naoufal/react-native-touch-id#fallback)
- [Methods](https://github.com/naoufal/react-native-touch-id#methods)
- [Errors](https://github.com/naoufal/react-native-touch-id#errors)
- [License](https://github.com/naoufal/react-native-touch-id#license)

## Install
```shell
npm i --save react-native-touch-id
```

## Support
Due to the rapid changes being made in the React Native ecosystem, we are not officially going to support this module on anything but the latest version of React Native. The current supported version is indicated on the React Native badge at the top of this README.  If it's out of date, we encourage you to submit a pull request!

## Usage
### Linking the Library
In order to use Touch ID, you must first link the library to your project.  There's excellent documentation on how to do this in the [React Native Docs](http://facebook.github.io/react-native/docs/linking-libraries-ios.html#content).

### Requesting Touch ID Authentication
Once you've linked the library, you'll want to make it available to your app by requiring it:

```js
var TouchID = require('react-native-touch-id');
```

Requesting Touch ID authentication is as simple as calling:
```js
TouchID.authenticate('to demo this react-native component')
  .then(success => {
    // Success code
  })
  .catch(error => {
    // Failure code
  });
```

## Example
Using Touch ID in your app will usually look like this:
```js
var TouchID = require('react-native-touch-id');

var YourComponent = React.createClass({
  _pressHandler() {
    TouchID.authenticate('to demo this react-native component')
      .then(success => {
        AlertIOS.alert('Authenticated Successfully');
      })
      .catch(error => {
        AlertIOS.alert('Authentication Failed');
      });
  },

  render() {
    return (
      <View>
        ...
        <TouchableHighlight onPress={this._pressHandler}>
          <Text>
            Authenticate with Touch ID
          </Text>
        </TouchableHighlight>
      </View>
    );
  }
});
```

## Methods
### authenticate(reason)
Attempts to authenticate with Touch ID.
Returns a `Promise` object.

__Arguments__
- `reason` - An _optional_ `String` that provides a clear reason for requesting authentication.

__Examples__
```js
TouchID.authenticate('to demo this react-native component')
  .then(success => {
    // Success code
    console.log('User authenticated with TouchID');
  })
  .catch(error => {
    // Failure code
    console.log(error);
  });
```

### isSupported()
Verify's that Touch ID is supported.
Returns a `Promise` object.

__Examples__
```js
TouchID.isSupported()
  .then(supported => {
    // Success code
    console.log('TouchID is supported.');
  })
  .catch(error => {
    // Failure code
    console.log(error);
  });
```

## Errors
There are various reasons why authenticating with Touch ID may fail.  Whenever calling Touch ID authentication fails, `TouchID.authenticate` will return an error code representing the reason.

Below is a list of error codes that can be returned:

| Code | Description |
|---|---|
| `LAErrorAuthenticationFailed` | Authentication was not successful because the user failed to provide valid credentials. |
| `LAErrorUserCancel` | Authentication was canceled by the user—for example, the user tapped Cancel in the dialog. |
| `LAErrorUserFallback` | Authentication was canceled because the user tapped the fallback button (Enter Password). |
| `LAErrorSystemCancel` | Authentication was canceled by system—for example, if another application came to foreground while the authentication dialog was up. |
| `LAErrorPasscodeNotSet` | Authentication could not start because the passcode is not set on the device. |
| `LAErrorTouchIDNotAvailable` | Authentication could not start because Touch ID is not available on the device |
| `LAErrorTouchIDNotEnrolled` | Authentication could not start because Touch ID has no enrolled fingers. |
| `RCTTouchIDUnknownError` | Could not authenticate for an unknown reason. |
| `RCTTouchIDNotSupported` | Device does not support Touch ID. |

_More information on errors can be found in [Apple's Documentation](https://developer.apple.com/library/prerelease/ios/documentation/LocalAuthentication/Reference/LAContext_Class/index.html#//apple_ref/c/tdef/LAError)._

## License
Copyright (c) 2015, [Naoufal Kadhom](http://naoufal.com/)

Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
