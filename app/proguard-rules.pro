# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserva número de linha para o Crashlytics conseguir mapear stack traces
# via mapping.txt mesmo com o código ofuscado.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room, Mapbox e Firebase já trazem suas próprias consumer proguard rules
# dentro dos AARs. Koin resolve via tipo reificado em bytecode (sem
# reflection/scanning) e o Coil registra fetchers/decoders explicitamente no
# código — nenhum dos dois precisa de -keep hoje. Se aparecer
# NoDefinitionFoundException (Koin) ou uma classe faltando em runtime após
# habilitar R8, adicionar um -keep pontual para a classe específica em vez de
# um -keep amplo que anularia o shrinking.

# Se o projeto usar WebView com JS, descomentar e informar a classe:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}