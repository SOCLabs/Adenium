package com.adenium.parser.devices

import com.adenium.parser.devices

/** Handling Device type Category
  *
  * You can classify agent types and apply processing logic defined for each category.
  * normal : Regular expression based syslog.
  * Can be further defined.
    {{{
        Example
        val jsonType: devices.SOCDeviceKind.Value = Value // json expression based log server
        val logServers: devices.SOCDeviceKind.Value = Value // custom log server
        val monitorType: devices.SOCDeviceKind.Value = Value // System status log type
    }}}
  */
object SOCDeviceKind extends Enumeration {

  type SOCDeviceKind = Value

  val normal: devices.SOCDeviceKind.Value = Value

}
