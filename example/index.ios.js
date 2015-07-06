/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';

var React = require('react-native');
var {
  AppRegistry,
  StyleSheet,
  Text,
    AlertIOS,
    TouchableHighlight,
  View,
} = React;
var TouchID = require('./TouchID.ios');

var example = React.createClass({
    useTouchID: function(){
        TouchID.authenticate("Test Reason", function(error, success){
            if(error){
                AlertIOS.alert(
                    'Error',
                    "Touch ID error=" + error.message,
                    [
                        {
                            text: 'Ok'
                        }
                    ]
                )
            } else {
                AlertIOS.alert(
                    'Success',
                    "Authentication Successful",
                    [
                        {
                            text: 'Ok'
                        }
                    ]
                )
            }
        })
    },
  render: function() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native TouchID
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.ios.js
        </Text>
        <Text style={styles.instructions}>
          Press Cmd+R to reload,{'\n'}
          Cmd+D or shake for dev menu
        </Text>
          <TouchableHighlight
              style = {styles.button}
              onPress = {this.useTouchID}>
              <Text style={styles.buttonText}> Authenticate with TouchID </Text>
          </TouchableHighlight>
      </View>
    );
  }
});

var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
    button:{
        padding:10,
        marginTop: 20,
        backgroundColor: "#24c3c8",
    },
    buttonText:{
        fontWeight: "bold",
        fontSize: 20,
        color: "white",
    }

});

AppRegistry.registerComponent('example', () => example);
