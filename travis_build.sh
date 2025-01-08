#!/bin/bash

generate_parent_pom(){
    header=$(<msi.gama.experimental.parent/pom_header.xml)
    current_modules=$(<msi.gama.experimental.parent/pom_modules.xml)
    footer=$(<msi.gama.experimental.parent/pom_footer.xml)

    modules=$'\n'$"<modules>"$'\n'
    for file in *; do 
      if [[ -d "$file" && ! -L "$file" ]]; then
        #echo "$file is a directory"; 
         if [[ -f "$file/pom.xml" && "$file" != "msi.gama.experimental.parent" ]]; then
            echo "File $file/pom.xml found!"        
            modules="$modules <module>../$file</module> "$'\n'
        fi
      fi; 
    done

    modules="$modules </modules>"$'\n'
    if [[ "$current_modules" != "$modules" ]]; then
        echo "$modules" > msi.gama.experimental.parent/pom_modules.xml
        echo "$header $modules $footer"> msi.gama.experimental.parent/pom.xml
    fi
}
generate_p2updatesite_category(){
    header=$(<msi.gama.experimental.p2updatesite/category_header.xml)
    user_cate=$(<msi.gama.experimental.p2updatesite/category_body_user.xml)
    current_cate=$(<msi.gama.experimental.p2updatesite/category_body.xml)
    footer=$(<msi.gama.experimental.p2updatesite/category_footer.xml)

    cate=$'\n'$" "$'\n'
    for file in *; do 
      if [[ -d "$file" && ! -L "$file" ]]; then
		isInFile=$( cat msi.gama.experimental.p2updatesite/category_body_user.xml | grep -c ${file})
         if [[ -f "$file/pom.xml" && ${file} != *"msi.gama.experimental.parent"* &&  $isInFile -eq 0  ]]; then
            
            if [[ ${file} == *"feature"* ]]; then	
               
                version=$(sed '/<parent>/,/<\/parent>/d;/<version>/!d;s/ *<\/\?version> *//g' "$file/pom.xml")

                q=$".qualifier"
                version=${version/-SNAPSHOT/$q}
				temp="<feature  url=\"features/"$file"_$version.jar\" id=\"$file\" version=\"$version\"> <category name=\"gama.optional\"/>   </feature>"
				temp=$(echo $temp|tr -d '\r')
				temp=$(echo $temp|tr -d '\n')
                cate="$cate $temp "$'\r'$'\n'        
				
                #echo $cate
				echo 
                
            fi
        fi
      fi; 
    done
    
    if [[ "$current_cate" != "$cate" ]]; then
        echo "$cate" > msi.gama.experimental.p2updatesite/category_body.xml
        echo "$header $cate $user_cate $footer " > msi.gama.experimental.p2updatesite/category.xml
    fi
}

generate_parent_pom
generate_p2updatesite_category

cd msi.gama.experimental.parent &&

if [[ $GPG_PASSPHRASE != "" ]]; then
    mvn verify -Dgpg.passphrase=$GPG_PASSPHRASE --settings ../settings.xml 
fi

mvn clean install -Dtycho.p2.transport.min-cache-minutes=0 -Dtycho.equinox.resolver.uses=true -P p2Repo --settings ../settings.xml &&
cd -
