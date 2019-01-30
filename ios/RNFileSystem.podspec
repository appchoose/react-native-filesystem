require 'json'
package = JSON.parse(File.read('./package.json'))

Pod::Spec.new do |s|
  s.name                = "RNFileSystem"
  s.version             = package["version"]
  s.summary             = package["description"]
  s.homepage            = "https://github.com/tgensol/react-native-filesystem.git"
  s.license             = package['license']
  s.authors             = "Blend"
  s.description         = <<-DESC
                           Webp Support for react native
                          DESC
  s.source              = { :git => "https://github.com/tgensol/react-native-filesystem.git", :tag => "v#{s.version}" }
  s.platform            = :ios, "9.0"
  s.source_files        = 'RNFileSystem/**/*.{h,m}'
  s.dependency          'React'
end

