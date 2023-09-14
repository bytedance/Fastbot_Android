# Fastbot-Android Open Source Handbook

## Introduction
> Fastbot is a model-based testing tool for modeling GUI transitions to discover app stability problems. It combines machine learning and reinforcement learning techniques to assist discovery in a more intelligent way.

> Related:  [Fastbot-iOS](https://github.com/bytedance/Fastbot_iOS)

***More detail see at [Fastbot architecture](https://mp.weixin.qq.com/s/QhzqBFZygkIS6C69__smyQ)

## Features
* Fastbot is compatible with multiple Android OS systems, including original Android, Android 5-14 and a variation of modified Andriod-based system by domestic manufacturers.
* Inherited from original Monkey, Fastbot allows for fast action insertion as high as 12 actions per second.
* Expert system is equipped with the ability to customize deeply based on needs from different business lines.
* Fastbot is a model-based-testing tool. Model is build via graph transition with the consideration of high reward choice selection.

**update 2023.9**
* Add Fastbot code analysis file for quick understanding of the source code. You can find it [here](./fastbot_code_analysis.md).

**update 2023.8**
* Java & Cpp code are fully open-sourced, feel free to build/extend Fastbot on your own (supported by and collaborated with [Prof. Ting Su](https://mobile-app-analysis.github.io/)'s research group from East China Normal University). Welcome any code or idea contribution!

**update 2023.3**
* support android 13

**update 2022.1**
* update Fastbot Revised License

**update 2021.11**
* support android 12
* add some new GUI fuzzing & mutation features (inspired/supported by [Themis](https://github.com/the-themis-benchmarks/home))

**update 2021.09**
* Fastbot supports model reuse: see at `/sdcard/fastbot_[packagename].fbm`. This file is loaded by default if it exists when Fastbot starts. During execution, it is overwritten every 10 minutes. The user can delete or copy this file based on their needs.


## Build
The specific method of compiling Fastbot's apk file monkey.apk.

The compilation of this project depends on gradle, so please install gradle first. Since there are many versions of gradle, the compatibility between different versions is different, so it is recommended to use sdkman to download and manage different versions of gradle. For specific installation and use of sdkman, please refer to: https://sdkman.io/

In short, to install sdkman, execute the following command in the shell:

```shell
curl -s "https://get.sdkman.io" | bash
```

After installing sdkman, please cd to the Fastbot project folder to open the shell, and execute the following command in the shell:

```shell
sdk install gradle 7.6.2 
```
```shell
gradle wrapper
```

This project relies on ndk and cmake. After installing gradle, please install the SDK required for Android development and execute the following command to install the specific version of ndk and cmake required by this project. Of course, you can also modify the build.gradle file in the monkey directory to modify the versions of ndk and cmake to the versions in your development environment.

```shell
sdkmanager "cmake;3.18.1"
sdkmanager "ndk;25.2.9519653"
```

After that, enter the following command: 
```shell
./gradlew clean makeJar
~/Library/Android/sdk/build-tools/28.0.3/dx --dex --output=monkeyq.jar monkey/build/libs/monkey.jar
```

After the compilation process is over, you can see the monkeyq.jar file in the root directory. This file is the final compiled Fastbot java package.

After compiling the so file, run:
```shell
sh ./build_native.sh
```

After the compilation process, you can see the .so file in the libs directory. This file directory is the final compiled Fastbot so package.


## Usage
### Environment preparation
* Clone this repo, cd this repo and build monkeyq.jar, and please make sure the ndk and cmake are all well configured in your environment.
  ```shell
  ./gradlew clean makeJar
  sh ./build_native.sh
  ~/Library/Android/sdk/build-tools/28.0.3/dx --dex --output=monkeyq.jar monkey/build/libs/monkey.jar
  ```
* Push artifacts into your device.
  ```shell
  adb push monkey/build/libs/monkeyq.jar /sdcard/monkeyq.jar
  adb push fastbot-thirdpart.jar /sdcard/fastbot-thirdpart.jar
  adb push libs/* /data/local/tmp/
  adb push framework.jar /sdcard/framework.jar
  ```

### Run Fastbot with shell command
`
adb -s device_vendor_id shell CLASSPATH=/sdcard/monkeyq.jar:/sdcard/framework.jar:/sdcard/fastbot-thirdpart.jar exec app_process /system/bin com.android.commands.monkey.Monkey -p package_name --agent reuseq --running-minutes duration(min) --throttle delay(ms) -v -v
`
* before run the command，user can push the strings in apk to `/sdcard/` to improve the model
    * `aapt2` or `aapt` depends your android sdk, a sample aapt path is ``` ${ANDROID_HOME}/build-tools/28.0.2/aapt2```

  ```shell
  aapt2 dump  --values strings  [testApp_path.apk] > max.valid.strings
  adb push max.valid.strings /sdcard 
  ```

> For more Details,  please refer to the handbook in [中文手册](./handbook-cn.md)

#### required parameters

``` shell
-s device_vendor_id # if multiple devices allowed, this parameter is needed; otherwise just optional
-p package_name # app package name under test, the package name for the app under test can be acquired by "adb shell pm list package", once the device is ensured for connection by "adb devices"
--agent robot # strategy selected for testing, no need to modify
--running-minutes duration # total amount time for testing
--throttle delay # time lag between actions
```

#### optional parameters
``` shell
--bugreport # log printed when crash occurs
--output-directory /sdcard/xxx # folder for output directory
```

#### optional fuzzing data
``` shell
adb push data/fuzzing/ /sdcard/
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/fuzzing
```

### Results Explanation
#### Observed crash and ANR
* Observed Java crash, ANR and native crash will be written into /sdcard/crash-dump.log
* Observed ANR will be written into /sdcard/oom-traces.log


#### Activity coverage data
* Total activity list will be printed in shell after Fastbot job done, together with explored activity list and rate of coverage in this job run.
* Equation for total activity coverage:  coverage = exploredActivity / totalActivity * 100%
* Be aware for totalActivity: The list totalActivity is acquired through framework interface PackageManager.getPackageInfo. Contained activities in the list includes many abandoned, invisible or not-reachable activities.

### Code Analysis and Extension
#### Basic framework
Fastbot-Android comprises Java and C++ code. The Java codebase is located in the "monkey" directory, while the C++ codebase resides in the "native" directory. The Java code is implemented on the basis of Monkey. Its primary role is to interact with Android devices and the local server, and pass GUI information to the Native layer. The Native layer then computes the Action with the highest expected reward for the next step and returns it to the client as an Operate object which is formatted as JSON. 
#### Extension

To extend Fastbot, you can make enhancements to both the Java layer and the C++ layer.
>For more details, please refer to the [fastbot code analysis](./fastbot_code_analysis.md) file.


## Acknowledgement
* We appreciate the insights and code contribution by Prof. Ting Su (East China Normal University)、Dr. Tianxiao Gu and Prof. Zhendong Su (ETH Zurich) etc.
* We thank the useful discussions with Prof. Yao Guo (PKU) on Fastbot.
* We want to express our gratitude to Prof. Zhenhua Li (THU) and Dr. Liangyi Gong (THU) for their helpful opinions on Fastbot.
* We are also grateful for valuable advices from Prof. Jian Zhang (Chinese Academy of Sciences).


## Publications

If you use our work in your research, please kindly cite us as:

1. Lv, Zhengwei, Chao Peng, Zhao Zhang, Ting Su, Kai Liu, Ping Yang (2022). “Fastbot2: Reusable Automated Model-based GUI Testing for Android Enhanced by Reinforcement Learning”. In proceedings of the 37th IEEE/ACM International Conference on Automated Software Engineering (ASE 2022). ACM, To appear. [[pdf]](https://se-research.bytedance.com/pdf/ASE22.pdf)

```bibtex
@inproceedings{fastbot2,
  title={Fastbot2: Reusable Automated Model-based GUI Testing for Android Enhanced by Reinforcement Learning},
  author={Lv, Zhengwei and Peng, Chao and Zhang, Zhao and Su, Ting and Liu, Kai and Yang, Ping},
  booktitle={Proceedings of the 37th IEEE/ACM International Conference on Automated Software Engineering (ASE 2022)},
  year={2022}
}
```

2. Peng, Chao, Zhao Zhang, Zhengwei Lv, Ping Yang (2022). “MUBot: Learning to Test Large-Scale Commercial Android Apps like a Human”. In proceedings of the 38th International Conference on Software Maintenance and Evolution (ICSME 2022). IEEE, To appear. [[pdf]](https://se-research.bytedance.com/pdf/ICSME22B.pdf)

```bibtex
@inproceedings{mubot,
  title={MUBot: Learning to Test Large-Scale Commercial Android Apps like a Human},
  author={Peng, Chao and Zhang, Zhao and Lv, Zhengwei and Yang, Ping},
  booktitle={Proceedings of the 38th International Conference on Software Maintenance and Evolution (ICSME 2022)},
  year={2022}
}
```

3. Cai, Tianqin, Zhao Zhang, and Ping Yang. “Fastbot: A Multi-Agent Model-Based Test Generation System”. In Proceedings of the IEEE/ACM 1st International Conference on Automation of Software Test. 2020. [[pdf]](https://se-research.bytedance.com/pdf/AST20.pdf)

```bibtex
@inproceedings{fastbot,
  title={Fastbot: A Multi-Agent Model-Based Test Generation System},
  author={Cai, Tianqin and Zhang, Zhao and Yang, Ping},
  booktitle={Proceedings of the IEEE/ACM 1st International Conference on Automation of Software Test},
  pages={93--96},
  year={2020}
}
```

## Contributors
Zhao Zhang, Jianqiang Guo, Yuhui Su, Tianxiao Gu, Zhengwei Lv, Tianqin Cai, Chao Peng, Bao Cao, Shanshan Shao, Dingchun Wang, Jiarong Fu, Ping Yang, Ting Su, Mengqian Xu

Welcome more one to become contributors