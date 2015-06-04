# react-native-touch-id

[![npm version](https://img.shields.io/npm/v/react-native-touch-id.svg?style=flat-square)](https://www.npmjs.com/package/react-native-touch-id)
[![npm downloads](https://img.shields.io/npm/dm/react-native-touch-id.svg?style=flat-square)](https://www.npmjs.com/package/react-native-touch-id)

__`react-native-touch-id`__ is a React Native library for authenticating users with Touch ID.

![react-native-touch-id](https://cloud.githubusercontent.com/assets/1627824/7975919/2c69a776-0a42-11e5-9773-3ea1c7dd79f3.gif)

## Documentation
- [Install](https://github.com/naoufal/react-native-touch-id#install)
- [Usage](https://github.com/naoufal/react-native-touch-id#usage)
- [Example](https://github.com/naoufal/react-native-touch-id#example)
- [Methods](https://github.com/naoufal/react-native-touch-id#methods)
- [Errors](https://github.com/naoufal/react-native-touch-id#errors)
- [Todo](https://github.com/naoufal/react-native-touch-id#todo)
- [License](https://github.com/naoufal/react-native-touch-id#license)

## Install
```shell
npm i --save react-native-touch-id
```

## Usage
### Linking the Library
In order to use Touch ID, you must first link the library your project.  There's excellent documentation on how to do this in the [React Native Docs](https://facebook.github.io/react-native/docs/linking-libraries.html#content).

### Requesting Touch ID Authentication
Once you've linked the library, you'll want to make it available to your app by requiring it:

```js
var TouchID = require('react-native-touch-id');
```

Requesting Touch ID authentication is as simple as calling:
```js
TouchID.authenticate(function(error, success) {
  // Your lovely code
});
```

## Example
Using Touch ID in your app will usually look like this:
```js
var TouchID = require('react-native-touch-id');

var YourComponent = React.createClass({
  _pressHandler() {
    TouchID.authenticate(function(error, success) {
      if (error) {
        // Failure code
      } else {
        // Success code
      }
    });
  },

  render() {
    return (
      <View>
        ...
        <TouchableHighlight
          onPress={this._pressHandler}
        />
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
### authenticate(callback)
Returns a Touch ID authentication success or error.  If there was a problem authenticating, and `error` object will be returned with the error reason.

__Arguments__
- `callback` - A _Function_ with `error` and `success` arguments.

__Examples__
```js
TouchID.authenticate(function(error, success) {
  if (error) {
    // Failure code
    console.log(error.message);
  } else {
    // Success code
    console.log('User authenticated with TouchID');
  }
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

## Todo
- [ ] Add `authReason` argument
- [ ] Promisify `authenticate` method
- [ ] Return better `error` objects

## License
Copyright (c) 2015, Naoufal Kadhom

Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
