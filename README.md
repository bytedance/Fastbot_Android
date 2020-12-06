# Fastbot-Android Open Source Handbook

## Introduction
> Fastbot is a model-based testing tool for modeling GUI transitions to discover app stability problems. It combines machine learning and reinforcement learning techniques to assist discovery in a more intelligent way. GUI state abstraction in Fastbot is achieved with reference to project APE.

## Features
* Fastbot is compatible with multiple Android OS systems, including original Android, Android 5-11 and a variation of modified Andriod-based system by domestic manufacturers.
* Inherited from original Monkey, Fastbot allows for fast action insertion as high as 12 actions per second.
* Expert system is equipped with the ability to customize deeply based on needs from different business lines.
* Fastbot is a model-based-testing tool. Model is build via graph transition with the consideration of high reward choice selection.
* Fastbot supports non-standard widgets by computer vision techniques such as YOLOv3, ocr and cv segmentation.

> Fastbot-iOS: Under construction

## Usage
### Environment preparation
* Make sure Android version on your device or emulator is Android 5, 6, 7, 8, 9, 10, 11
* Push framework.jar and monkeyq.jar into your device, most likely /sdcard
```
adb push framework.jar /sdcard
adb push monkeyq.jar /sdcard
```

### Run Fastbot with shell command
`
adb -s device_vendor_id shell CLASSPATH=/sdcard/monkeyq.jar:/sdcard/framework.jar exec app_process /system/bin
com.android.commands.monkey.Monkey -p package_name --agent robot --running-minutes duration(min) --throttle delay(ms) -v -v
`

#### required parameters

```
-s device_vendor_id # if multiple devices allowed, this parameter is needed; otherwise just optional
-p package_name # app package name under test, the package name for the app under test can be acquired by "adb shell pm list package", once the device is ensured for connection by "adb devices"
--agent robot # strategy selected for testing, no need to modify
--running-minutes duration # total amount time for testing
--throttle delay # time lag between actions
```

#### optional parameters
```
--bugreport # log printed when crash occurs
--output-directory /sdcard/xxx # folder for output directory
```

### Results Explanation
#### Observed crash and ANR
* Observed Java crash, ANR and native crash will be written into /sdcard/crash-dump.log
* Observed ANR will be written into /sdcard/oom-traces.log


#### Activity coverage data
* Total activity list will be printed in shell after Fastbot job done, together with explored activity list and rate of coverage in this job run.
* Equation for total activity coverage:  coverage = exploredActivity / totalActivity * 100%
* Be aware for totalActivity: The list totalActivity is acquired through framework interface PackageManager.getPackageInfo. Contained activities in the list includes many abandoned, invisible or not-reachable activities.


#### Acknowledgement
* We appreciate the insights and code contribution from project APE by Dr. Tianxiao Gu and Prof. Zhendong Su (ETH Zurich) etc.
* We thank the useful discussions with Prof. Yao Guo (PKU) on Fastbot.
* We want to express our gratitude to Prof. Zhenhua Li (THU), Dr. Liangyi Gong (THU) and Prof. Ting Su (East China Normal University) for their helpful opinions on Fastbot.
* We are also grateful for valuable advices from Prof. Jian Zhang (Chinese Academy of Sciences).

> For more Details,  please refer to the handbook in Chinese version

