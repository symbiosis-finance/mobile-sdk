Pod::Spec.new do |spec|
    spec.name                     = 'SymbiosisSDK'
    spec.version                  = '1.1.0'
    spec.homepage                 = '-'
    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = 'IceRock Development'
    spec.license                  = ''
    spec.summary                  = 'SymbiosisSDK library'
    spec.module_name              = "#{spec.name}"

    spec.vendored_frameworks      = "build/XCFrameworks/release/SymbiosisSDK.xcframework"
    spec.ios.deployment_target    = '12.0'
    spec.swift_version            = '5.0'

    spec.dependency 'SwiftWeb3Wrapper'

    spec.pod_target_xcconfig = {
        'VALID_ARCHS' => '$(ARCHS_STANDARD_64_BIT)'
    }
end
