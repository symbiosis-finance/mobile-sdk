[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) 

![badge-android] ![badge-jvm]

# Symbiosis Sdk
This is a library using Kotlin Multiplatform technology, 
which allows for convenient handling of cross-chain swaps

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Contributing](#contributing)
- [License](#license)

## Features

We maintain this section separately on the website, see [full documentation](
https://docs.symbiosis.finance/developer-tools/symbiosis-mobile-sdk) here.

## Installation

1) Add the following snippet to your repositories
```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/symbiosis-finance/mobile-sdk")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("TOKEN")
        }
    }
}
```
where 
`GITHUB_USERNAME` - your github username, that has access to the repository,
`TOKEN` - your github's personal access token, there is a [guide](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) of how to get it, you need the following scopes: `repo`, `workflow`, `read:packages`.

2) Add the following code to the module where you want to use sdk:
```groovy
dependencies {
    commonMainApi("com.symbiosis.sdk:sdk:2.0.9")
}
```

## Contributing
All development (both new features and bug fixes) is performed in `develop` branch. This way `master` sources always contain sources of the most recently released version. Please send PRs with bug fixes to `develop` branch. Fixes to documentation in markdown files are an exception to this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` during release.

More detailed guide for contributors see in [contributing guide](CONTRIBUTING.md).

## License
        
    Copyright 2021 Symbiosis Labs Ltd
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[badge-android]: http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat
[badge-ios]: http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat
[badge-linux]: http://img.shields.io/badge/platform-linux-2D3F6C.svg?style=flat
[badge-windows]: http://img.shields.io/badge/platform-windows-4D76CD.svg?style=flat
[badge-mac]: http://img.shields.io/badge/platform-macos-111111.svg?style=flat
[badge-watchos]: http://img.shields.io/badge/platform-watchos-C0C0C0.svg?style=flat
[badge-tvos]: http://img.shields.io/badge/platform-tvos-808080.svg?style=flat
[badge-wasm]: https://img.shields.io/badge/platform-wasm-624FE8.svg?style=flat
[badge-nodejs]: https://img.shields.io/badge/platform-nodejs-68a063.svg?style=flat
[badge-iosx64]: https://img.shields.io/badge/platform-iosx64-CDCDCD?style=flat
[badge-iosarm64]: https://img.shields.io/badge/platform-iosarm64-CDCDCD?style=flat
[badge-macos64]: https://img.shields.io/badge/platform-macos64-111111?style=flat    
