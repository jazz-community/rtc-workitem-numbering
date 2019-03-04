## Introduction
In Rational Team Concert(RTC), the workitem ID is assigned as the unique identifier of workitems. However this identifier is unique in the RTC server, so it's not sequential in each project area and awkward for workitem management.
The _Workitem Numbering follow-up action_ resolves this problem. This feature number the unique identifier by workitem type of the project area and save it as Work Item Number (Custom attribute).


**Key features**

1. Functions of the server side plugin
  - Automatic Numbering
This plugin automatically number the unique identifier by workitem type of the project area. This unique identifier is sequential in each workitem type.
  -	Automatic Roatation of Workitem Number
When the final Workitem Number reaches the max value with the number of digit which is configured in "Configuration of the number of digit",the next Workitem Number is set to "1" again.
Avoiding duplication of Workitem Numbers, set the number of digit larger.
2.	Functions of the RTC Eclipse Client plugin
	 - Configuration of the number of digit
To be usefu in the listing of workitems, this plugin supports zero-padding.
For instance, the number of digit is "5", Workitem Number is assigned "00001","00002","00003" in order.
Options for the number of digit is mutable with the configuration file for this plugin.In default configuration is "5-digit" or "10-digit" or "15-digit".
	 - Additional numbering key with custom attributes
Normally, Workitem Number is numbered by workitem type of the project area. When this workitem has some custom attributes, these custom attributes is available for the additional numbering keys
For instance, When "Defect" workitem type has "al phase", "causal phase" is available for the additional numbering key. And Workitem Number is numbered by "causal phase".
    - On & Off control
      1. On & Off control for the server plugin
This plugin is controlled with the process configuration of the project area because this plugin extends the follow-up action feature.
      2.	On & Off control for the target workitem type
The project area editor is available for On & Off control for the target workitem type. For additional information, refer "Configuration of target workitem types"
    - Validating the configuration of "Workitem Number attribute"
It is a validation function for the configuration of "Workitem Number attribute". When the attribute isn't configured in a workitem type correctly, this plugin don't allow users to turn on the feature with that workitem type.

## Installation and Usage
There are detailed instructions on how to install and use the plugin in the [MS Word doc](https://github.com/jazz-community/rtc-workitem-numbering/blob/master/Guide%20for%20Workitem%20Numbering%20follow-up%20action_en.docx) of this repository.

## Contributing
Please use the [Issue Tracker](https://github.com/jazz-community/rtc-workitem-numbering/issues) of this repository to report issues or suggest enhancements.

For general contribution guidelines, please refer to [CONTRIBUTING.md](https://github.com/jazz-community/welcome/blob/master/CONTRIBUTING.md).

## Licensing
Copyright (c) IBM Corporation. All rights reserved.<br>
Licensed under the [MIT](https://github.com/jazz-community/rtc-workitem-numbering/blob/master/LICENSE) License.
