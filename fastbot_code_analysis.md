# 中文版本
基本框架
Fastbot-Android代码分为Java层和Native层（C++），Java代码位于monkey文件目录下，C++代码位于native目录下。Java层在Monkey的基础上实现，主要负责与安卓设备以及本地服务端的交互，并将页面信息传递给Native层。然后由Native层接收并计算出下一步收益最大的Action，并转换为Operate返回给客户端。项目的目录结构如下：
```
.
├── monkey
│   ├── libs
│   └── src
│       └── main
│           ├── java.com.android.commands.monkey
│                   │    └── monkey
│                   │       ├── action
│                   │       ├── events
│                   │       │   ├── base
│                   │       │   │   └── mutation
│                   │       │   └── customize
│                   │       ├── fastbot
│                   │       │   └── client
│                   │       ├── framework
│                   │       ├── provider
│                   │       ├── source 
│                   │       ├── tree
│                   │       └── utils
│                   │       └── Monkey.java       
│                   └── bytedance
│                       └── fastbot
└── native
    ├── agent 
    ├── desc 
    │   └── reuse  
    ├── events 
    ├── model 
    ├── project
    │   └── jni
    ├── storage 
    └── thirdpart
```
## Java层
Fastbot的执行入口在Monkey.java，主循环代码位于`runMonkeyCycles`函数里。
- source目录下存放了Monkey的几个事件源，其中`MonkeySourceApeNative`产生Fastbot事件源，它调用`AiClient`里的jni方法与本地代理服务连接，获取下一步需要执行的动作，并进行解析。
- tree目录下的`TreeBuilder`类，负责从被测App中获取相应的GUI 树信息，将`AccessibilityNodeInfo`对象转换为 XML 字符串表示形式。
- action目录下存放了以`Action`作为父类的其他派生类，负责存储模型算法返回的Action信息。
- event目录下存放了Fastbot封装的事件类，其中customize子目录下存放Fastbot自定义事件，这些事件完成了对Monkey原生事件的封装，允许用户通过配置文件自定义相关的操作。base子目录下存放Monkey原生的各种事件类。
- framework目录下存放框架级别代码和类，对底层系统的抽象和封装，提供与安卓设备处理相关的操作，以及兼容性和版本的适配。
- provider目录下存放为其他组件或应用程序提供特定功能或资源的provider类，其中包括`SchemaProvider`和`ShellProvider`类，分别用于提供数据模式和Shell命令。
- utils目录下存放与通用功能和工具相关的类，包括配置文件信息的读取，日志文件的记录，Activity的过滤等相关工具类。
- fastbot.cilent 目录下存放于服务端交互的数据类型，包括定义操作类型的枚举类`Actiontype`、封装客户端操作属性和参数，以及提供json数据的转换和处理的`Operate`类。（服务端返回json格式的operate）
- bytedance.fastbot目录下存放一个`AiClient`类，定义了与服务端交互的JNI方法。
## Native层
Native层中的几个主要的文件：
1. fastbot_native.cpp
位于project.jni文件，该文件为上层的Java层提供了JNI的接口实现。其中`b0bhkadf`函数提供了决策的核心功能。
2. Model.cpp
Model对象的`getOperate`方法是能够让其他cpp代码调用强化学习模型的公有函数，此函数的调用链依赖于Model的`getOperateOpt`方法。该方法以当前页面的XML信息、Class的类名（Activity的名称）以及当前测试设备的设备ID作为输入，返回包含了决策信息的Json对象。
3. Graph.cpp
Model使用了Graph对象来记录活动转移图（Activity Transition Graph）信息。一个Activity被视为一个State，一个State中包含了一定数量的组件，通过在组件上执行不同类型的动作，即Action后，可以跳转到新的State。
4. 各种决策代码文件
  1. AbstractAgent.cpp：此文件是所有Agent的基类，规定了做决策的基本通用逻辑。
  2. ReuseAgent.cpp：此文件实现了强化学习的sarsa n-step的agent，并且支持模型重用。
## 数据结构
Fastbot中的几个关键的数据结构，Action，Operate，State，Graph，Agent和Model。
- Action表示模型中的动作，每个Action都有一个ActionType表示动作的类型。
- Operate表示设备可以理解的操作，是对Action的包装，通过Action对象的`toOperate`方法转换为设备可理解的执行操作。Native层通过`deviceoperatewrapper`类将模型生成的Action包装为一个operate，然后以Json格式返回给Java层。
- State表示抽象的App状态，存储被测应用的GUI页面信息。
- Graph表示模型探索到的App状态地图，图中的每个节点是State，以Action为边链接相邻的State。
- Agent表示模型中用于做出决策的智能体，他计算当前state下每个Action的收益。
- Model表示模型，用于统筹管理和分配每个设备的决策智能体，同时获取当前的graph信息传递给agent进行决策，并返回最终的决策结果。
## 扩展功能
如果你想要扩展 Fastbot，可以从 Java 层和 C++ 层两个方面进行：
- 扩展 Fastbot 的决策模型：
  - 在 C++ 层新增算法实现：Model类定义了调用算法的公有函数`getOperate`，通过继承Model类可以扩展新的决策模型。
  - 新增相应的JNI方法，实现Java和Native层的通信接口。
  - 在 Java 层，继承原生的 `MonkeyEvent`或者`MoneySourceApeNative`类，新增相应的事件源类以请求和解析算法返回的操作，并在主逻辑文件 Monkey.java 中新增相应的处理逻辑。
- 扩展 Fastbot 的客户端功能：
  - 在Java层修改 Utils 目录下的工具类来扩展通用功能。
  - 在Monkey.java的`processOptions`方法中新增命令行选项。
  - 在`run`和`runMonkeyCycle`方法的主循环中新增对应的处理逻辑适应新增的功能模块。


# English version
## Basic framework
Fastbot-Android comprises Java and C++ code.  The Java codebase is located in the "monkey" directory, while the C++ codebase resides in the "native" directory.
The Java code is implemented on the basis of Monkey. Its primary role is to interact with Android devices and the local server, and pass GUI information to the Native layer. The Native layer then computes the Action with the highest expected reward for the next step and returns it to the client as an Operate object which is formatted as JSON.  The directory structure of the project is as follows:
```
.
├── monkey
│   ├── libs
│   └── src
│       └── main
│           ├── java.com.android.commands.monkey
│                   │    └── monkey
│                   │       ├── action
│                   │       ├── events
│                   │       │   ├── base
│                   │       │   │   └── mutation
│                   │       │   └── customize
│                   │       ├── fastbot
│                   │       │   └── client
│                   │       ├── framework
│                   │       ├── provider
│                   │       ├── source 
│                   │       ├── tree
│                   │       └── utils
│                   │       └── Monkey.java       
│                   └── bytedance
│                       └── fastbot
└── native
    ├── agent 
    ├── desc 
    │   └── reuse  
    ├── events 
    ├── model 
    ├── project
    │   └── jni
    ├── storage 
    └── thirdpart
```
## Java Layer
The execution entry point of Fastbot is Monkey.java, and the main loop code is located in the `runMonkeyCycles` function.
- The source directory contains several event sources for Monkey. Among them, `MonkeySourceApeNative` generates Fastbot event sources. It calls JNI methods in `AiClient` to connect with the Native layer, retrieve the next action to be executed, and perform parsing.
- The `TreeBuilder` class in the tree directory is responsible for obtaining GUI tree information from the tested app. It converts AccessibilityNodeInfo objects into XML string representations.
- The action directory contains derived classes based on the `Action` class, which store the actions returned by the model algorithms.
- The event directory contains event classes encapsulated by Fastbot. The customize subdirectory contains Fastbot's custom events, which encapsulate the native Monkey events and allow users to customize related operations through configuration files. The base subdirectory contains various native Monkey event classes.
- The framework directory contains framework-level code and classes that abstract and encapsulate underlying system operations related to Android devices, as well as compatibility and version adaptation.
- The provider directory contains provider classes that provide specific functionality or resources for other components or applications. It includes the `SchemaProvider` and `ShellProvider` classes, which respectively provide data schemas and shell commands.
- The utils directory contains classes related to common functionalities and tools, including reading configuration file information, logging file records, activity filtering, and other related utility classes.
- The fastbot.client directory contains data types for server interactions, including the `Actiontype` enum class for defining action types, the `Operate` class for encapsulating client operation attributes and parameters, and handling JSON data (the server returns operate in JSON format).
- The bytedance.fastbot directory contains the `AiClient` class, which defines JNI methods for interacting with the server.
## Native Layer
Several key files in the Native layer:
1. fastbot_native.cpp:
This file, located in the project.jni directory, provides the JNI interface implementation for the upper-level Java layer. The function `b0bhkadf` provides the core functionality for decision-making.
2. Model.cpp:
The `getOperate` method of the Model object allows other CPP code to invoke the public function of the reinforcement learning model. The invocation chain of this function depends on the `getOperateOpt` method of the Model. This method takes the XML information of the current page, the class name (activity name) of the class, and the device ID of the current testing device as inputs and returns a JSON object containing decision information.
3. Graph.cpp:
The Model utilizes the Graph object to record activity transition graph information. An activity is treated as a state, and a state contains a certain number of components. By performing different types of actions on the components, transitions to new states can be made.
4. Various decision code files:
  1. AbstractAgent.cpp: This file serves as the base class for all Agents and defines the basic common logic for decision-making.
  2. ReuseAgent.cpp: This file implements the sarsa n-step agent for reinforcement learning and supports model reuse.
## Data Structures
Fastbot utilizes several key data structures: Action, Operate, State, Graph, Agent, and Model.
- Action represents actions within the model, where each Action has an ActionType that denotes the type of action.
- Operate represents operations that the device can understand. It serves as a wrapper for Action and can be transformed into device-executable operations using the `toOperate` method of the Action object. In the Native layer, the `deviceoperatewrapper` class wraps the Actions generated by the model into an Operate object, which is then returned to the Java layer in JSON format.
- State represents an abstract app state and stores GUI page information of the tested application.
- Graph represents the app state map discovered by the model. Each node in the graph represents a State, and adjacent States are connected by Actions.
- Agent represents an intelligent agent within the model responsible for making decisions. It calculates the payoff for each Action under the current State.
- Model represents the overall model, responsible for managing and allocating decision-making agents for each device. It retrieves the current graph information and passes it to the Agent for decision-making. The Model returns the final decision as the result.
## Extending Functionality
To extend Fastbot, you can make enhancements to both the Java layer and the C++ layer:
- Extending Fastbot's decision models:
  - Add algorithm implementations at the C++ layer: The `Model` class defines a public function `getOperate`, which invokes the algorithm. You can extend new decision models by inheriting from the Model class.
  - Add corresponding JNI methods to establish communication interfaces between Java and the Native layer.
  - In the Java layer, inherit from the native `MonkeyEvent` or `MonkeySourceApeNative` classes and create new event source classes to request and parse the operations returned by the algorithm. Add the corresponding handling logic to the main logic file Monkey.java.
- Extending Fastbot's client functionality:
  - Modify the utility classes under the Utils directory in the Java layer to extend common functionalities.
  - Add command-line options in the `processOptions` method of Monkey.java.
  - In the main loops of the `run` and `runMonkeyCycle` methods, add corresponding handling logic to accommodate the newly added functionality modules.
