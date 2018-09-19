'use strict';
import React, { Component } from 'react';
import {
  AlertIOS,
  AppRegistry,
  StyleSheet,
  Text,
  TouchableHighlight,
  View
} from 'react-native';

import TouchID from "react-native-touch-id";

class TouchIDExample extends Component {
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          react-native-touch-id
        </Text>

        <Text style={styles.instructions}>
          github.com/naoufal/react-native-touch-id
        </Text>
        <TouchableHighlight
          style={styles.btn}
          onPress={this._clickHandler}
          underlayColor="#0380BE"
          activeOpacity={1}
        >
          <Text style={{
            color: '#fff',
            fontWeight: '600'
          }}>
            Authenticate with Touch ID
          </Text>
        </TouchableHighlight>
      </View>
    );
  }

  _clickHandler() {
    TouchID.isSupported()
      .then(authenticate)
      .catch(error => {
        AlertIOS.alert('TouchID not supported');
      });
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF'
  },
  welcome: {
    margin: 10,
    fontSize: 20,
    fontWeight: '600',
    textAlign: 'center'
  },
  instructions: {
    marginBottom: 5,
    color: '#333333',
    fontSize: 13,
    textAlign: 'center'
  },
  btn: {
    borderRadius: 3,
    marginTop: 200,
    paddingTop: 15,
    paddingBottom: 15,
    paddingLeft: 15,
    paddingRight: 15,
    backgroundColor: '#0391D7'
  }
});

const errors = {
  "LAErrorAuthenticationFailed": "Authentication was not successful because the user failed to provide valid credentials.",
  "LAErrorUserCancel": "Authentication was canceled by the user—for example, the user tapped Cancel in the dialog.",
  "LAErrorUserFallback": "Authentication was canceled because the user tapped the fallback button (Enter Password).",
  "LAErrorSystemCancel": "Authentication was canceled by system—for example, if another application came to foreground while the authentication dialog was up.",
  "LAErrorPasscodeNotSet": "Authentication could not start because the passcode is not set on the device.",
  "LAErrorBiometryNotAvailable": "Authentication could not start because Biometry is not available on the device.",
  "LAErrorBiometryNotEnrolled": "Authentication could not start because Biometry has no enrolled biometric identities.",
  "LAErrorBiometryLockout": "Authentication could not start because Biometry had too many failed attempts.",
  "RCTTouchIDUnknownError": "Could not authenticate for an unknown reason.",
  "RCTTouchIDNotSupported": "Device does not support Touch ID."
};

function authenticate() {
  return TouchID.authenticate()
    .then(success => {
      AlertIOS.alert('Authenticated Successfully');
    })
    .catch(error => {
      console.log(error)
      AlertIOS.alert(error.message);
    });
}

function passcodeAuth() {
  return PasscodeAuth.isSupported()
    .then(() => {
      return PasscodeAuth.authenticate()
    })
    .then(success => {
      AlertIOS.alert('Authenticated Successfully');
    })
    .catch(error => {
      console.log(error)
      AlertIOS.alert(error.message);
    });
}

AppRegistry.registerComponent('TouchIDExample', () => TouchIDExample);
